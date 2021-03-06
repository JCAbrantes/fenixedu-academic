/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Academic.
 *
 * FenixEdu Academic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Academic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Academic.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.academic.task;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import org.fenixedu.academic.domain.QueueJob;
import org.fenixedu.academic.domain.QueueJobResult;
import org.fenixedu.academic.domain.QueueJobResultFile;
import org.fenixedu.academic.domain.QueueJobWithFile;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.messaging.core.domain.Message;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

@Task(englishTitle = "JobQueueDispatcher", readOnly = true)
public class JobQueueDispatcher extends CronTask {
    private static class QueueComparator implements Comparator<QueueJob> {
        @Override
        public int compare(final QueueJob queueJob1, final QueueJob queueJob2) {
            final Integer fc1 = queueJob1.getFailedCounter();
            final Integer fc2 = queueJob2.getFailedCounter();
            if (fc1 != null && fc2 != null) {
                final int result = fc1.compareTo(fc2);
                if (result != 0) {
                    return result;
                }
            }
            int result = queueJob1.getPriority().compareTo(queueJob2.getPriority());
            if (result != 0) {
                return result;
            }
            return queueJob1.getExternalId().compareTo(queueJob2.getExternalId());
        }
    }

    @Override
    public void runTask() {
        final Set<QueueJob> undoneJobs = Bennu.getInstance().getQueueJobUndoneSet();
        final QueueJob queueJob = undoneJobs.isEmpty() ? null : Collections.min(undoneJobs, new QueueComparator());
        if (queueJob != null) {
            try {
                taskLog("Started: %s (%s) requested by %s on %s\n", queueJob.getClass().getName(), queueJob.getExternalId(),
                        getQueueJobResponsibleName(queueJob), queueJob.getRequestDate());
                runJob(queueJob);
                taskLog("Finished Successfully\n");
            } catch (Throwable e) {
                fail(queueJob, e);
                taskLog("Failed %s times\n", queueJob.getFailedCounter());
                throw new Error(e);
            }
        }
    }

    public String getQueueJobResponsibleName(final QueueJob queueJob) {
        return queueJob.getPerson() == null ? "system" :
                String.format("%s(%s)", queueJob.getPerson().getName(), queueJob.getPerson().getUsername());
    }

    @Atomic(mode = TxMode.WRITE)
    protected void runJob(QueueJob job) throws Exception {
        job.setJobStartTime(new DateTime());
        QueueJobResult result = job.execute();
        if (job instanceof QueueJobWithFile) {
            final QueueJobWithFile queueJobWithFile = (QueueJobWithFile) job;
            queueJobWithFile.setContentType(result.getContentType());
            QueueJobResultFile.store(queueJobWithFile, queueJobWithFile.getPerson(), queueJobWithFile.getFilename(),
                    result.getContent());
        }
        job.setDone(true);
        job.setRootDomainObjectQueueUndone(null);
        job.setJobEndTime(new DateTime());
        if (job.getPerson() != null) {
            String subject = String.format("Pedido de %s concluido", job.getDescription());
            String body = String.format("O seu pedido de %s já se encontra disponível no sistema Fénix.", job.getDescription());
            Message.fromSystem()
                    .replyToSender()
                    .singleBcc(job.getPerson().getEmailForSendingEmails())
                    .subject(subject)
                    .textBody(body)
                    .send();
        }
    }

    @Atomic(mode = TxMode.WRITE)
    protected void fail(final QueueJob job, Throwable t) {
        job.setFailedCounter(job.getFailedCounter() + 1);
        job.setJobEndTime(new DateTime());
        if (job.getFailedCounter() == 3) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            String subject = String.format("Job %s failed 3 times", job.getClass().getName());
            String body =
                    "Viva\n\n" + "O trabalho com o externalId de " + job.getExternalId() + " falhou mais de 3 vezes.\n\n"
                            + "Request Time : " + job.getRequestDate() + "\n" + "Start Time : " + job.getJobStartTime() + "\n"
                            + "User : " + getQueueJobResponsibleName(job) + "\n" + "\n\n Error Stack Trace:\n" + sw.toString();
            Group tos = Group.managers();
            Message.fromSystem()
                    .to(tos)
                    .subject(subject)
                    .textBody(body)
                    .send();
        }
    }
}