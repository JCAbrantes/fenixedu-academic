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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.academic.domain.QueueJob;
import org.fenixedu.academic.domain.QueueJobWithFile;
import org.fenixedu.bennu.spring.portal.SpringApplication;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@SpringApplication(path = "thesis", hint = "Renates", group = "academic(MANAGE_AUTHORIZATIONS)", title = "title.thesis.report")
@SpringFunctionality(app = ThesisReportController.class, title = "title.thesis.report")
@RequestMapping("/thesis-test")
public class ThesisReportController {

    @Autowired
    private RenatesReportService renatesreportservice;

    @RequestMapping(method = RequestMethod.GET)
    public String list(Model model) {
        return getList(model, new ArrayList<String>());
    }

    @RequestMapping(value = "/requestRenatesReport", method = RequestMethod.GET)
    public String requestRenatesReport(Model model) {
        if (renatesreportservice.getUndoneThesisRenatesReportFiles().size() < 9) {
            renatesreportservice.createThesisRenatesReportFile();
        }
        return "redirect:/thesis-test";
    }

    @RequestMapping(value = "/download/renates-report/{file_name}", method = RequestMethod.GET)
    public String downloadExcel(@PathVariable("file_name") String fileName, Model model,
            HttpServletResponse httpServletResponse) {

        QueueJob queueJob = renatesreportservice.getThesisRenatesReportFile(fileName);

        try {
            if ((queueJob instanceof QueueJobWithFile) && ((QueueJobWithFile) queueJob).getFile() != null) {
                httpServletResponse.setContentType(((QueueJobWithFile) queueJob).getContentType());
                httpServletResponse.setHeader("Content-disposition", "attachment;filename=" + fileName);
                OutputStream outputStream;
                outputStream = httpServletResponse.getOutputStream();
                outputStream.write(((QueueJobWithFile) queueJob).getFile().getContent());
                outputStream.close();
            } else {
                List<String> errors = new ArrayList<String>();
                errors.add("label.renates.exceptions.file.not.found");
                return getList(model, errors);
            }
        } catch (IOException e) {
            List<String> errors = new ArrayList<String>();
            errors.add("label.renates.exceptions.ioexception");
            return getList(model, errors);
        }

        return "redirect:/thesis-test";

    }

    public String getList(Model model, List<String> errors) {
        model.addAttribute("doneQueuejobs", renatesreportservice.getDoneThesisRenatesReportFiles());

        final List<QueueJob> undoneJobs = renatesreportservice.getUndoneThesisRenatesReportFiles();
        model.addAttribute("undoneQueuejobs", undoneJobs);

        if (undoneJobs.size() >= 9) {
            errors.add("label.renates.exceptions.max.requests.reached");
        }
        model.addAttribute("errors", errors);
        return "fenixedu-academic/thesis/list";
    }

}