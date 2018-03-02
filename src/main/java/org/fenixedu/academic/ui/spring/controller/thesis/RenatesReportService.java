/**
 * Copyright © 2018 Instituto Superior Técnico
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

package org.fenixedu.academic.ui.spring.controller.thesis;

import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.QueueJob;
import org.fenixedu.academic.domain.thesis.ThesisRenatesReportFile;
import org.fenixedu.bennu.core.domain.Bennu;
import org.springframework.stereotype.Service;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

@Service
public class RenatesReportService {

    public List<QueueJob> getDoneThesisRenatesReportFiles() {
        return QueueJob.getLastJobsForClassOrSubClass(ThesisRenatesReportFile.class, 10).stream().filter(r -> r.getDone())
                .collect(Collectors.toList());
    }

    public List<QueueJob> getUndoneThesisRenatesReportFiles() {
        return QueueJob.getLastJobsForClassOrSubClass(ThesisRenatesReportFile.class, 10).stream().filter(r -> !r.getDone())
                .collect(Collectors.toList());
    }

    public ThesisRenatesReportFile getThesisRenatesReportFile(String fileName) {
        for (QueueJob queueJob : QueueJob.getLastJobsForClassOrSubClass(ThesisRenatesReportFile.class, 10)) {
            if (queueJob.getFilename().contains(fileName)) {
                return (ThesisRenatesReportFile) queueJob;
            }
        }

        List<QueueJob> queueJobs = Bennu.getInstance().getQueueJobSet().stream()
                .filter((ThesisRenatesReportFile.class)::isInstance).collect(Collectors.toList());

        for (QueueJob queueJob : queueJobs) {
            if (queueJob.getFilename().contains(fileName)) {
                return (ThesisRenatesReportFile) queueJob;
            }
        }

        return null;
    }

    @Atomic(mode = TxMode.WRITE)
    public void createThesisRenatesReportFile() {
        ThesisRenatesReportFile.buildRenatesReport();
    }

}
