package ServidorAplicacao.Servico.teacher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import DataBeans.ISiteComponent;
import DataBeans.InfoExam;
import DataBeans.InfoExamStudentRoomWithInfoStudentAndInfoRoom;
import DataBeans.InfoSiteCommon;
import DataBeans.InfoSiteTeacherStudentsEnrolledList;
import DataBeans.InfoStudent;
import DataBeans.TeacherAdministrationSiteView;
import Dominio.Exam;
import Dominio.ExamStudentRoom;
import Dominio.ExecutionCourse;
import Dominio.IExam;
import Dominio.IExamStudentRoom;
import Dominio.IExecutionCourse;
import Dominio.ISite;
import Dominio.IStudent;
import ServidorAplicacao.IServico;
import ServidorAplicacao.Factory.TeacherAdministrationSiteComponentBuilder;
import ServidorAplicacao.Servico.exceptions.FenixServiceException;
import ServidorPersistente.ExcepcaoPersistencia;
import ServidorPersistente.IPersistentExam;
import ServidorPersistente.IPersistentExamStudentRoom;
import ServidorPersistente.IPersistentExecutionCourse;
import ServidorPersistente.IPersistentSite;
import ServidorPersistente.ISuportePersistente;
import ServidorPersistente.OJB.SuportePersistenteOJB;

/**
 * @author Jo�o Mota
 *  
 */
public class ReadStudentsEnrolledInExam implements IServico {
    private static ReadStudentsEnrolledInExam service = new ReadStudentsEnrolledInExam();

    /**
     * The singleton access method of this class.
     */
    public static ReadStudentsEnrolledInExam getService() {
        return service;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ServidorAplicacao.IServico#getNome()
     */
    public String getNome() {
        return "ReadStudentsEnrolledInExam";
    }

    public Object run(Integer executionCourseCode, Integer examCode)
            throws FenixServiceException {
        try {

            ISuportePersistente sp = SuportePersistenteOJB.getInstance();
            IPersistentExam persistentExam = sp.getIPersistentExam();
            IPersistentExecutionCourse persistentExecutionCourse = sp
                    .getIPersistentExecutionCourse();
            IPersistentSite persistentSite = sp.getIPersistentSite();
            IPersistentExamStudentRoom examStudentRoomDAO = sp
                    .getIPersistentExamStudentRoom();

            IExecutionCourse executionCourse = (IExecutionCourse) persistentExecutionCourse
                    .readByOID(ExecutionCourse.class, executionCourseCode);
            ISite site = persistentSite.readByExecutionCourse(executionCourse);

            IExam exam = (IExam) persistentExam.readByOID(Exam.class, examCode);

            List examStudentRoomList = examStudentRoomDAO.readBy(exam);

            List infoExamStudentRoomList = (List) CollectionUtils.collect(
                    examStudentRoomList, new Transformer() {

                        public Object transform(Object input) {
                            ExamStudentRoom examStudentRoom = (ExamStudentRoom) input;
                            //CLONER
                            //return Cloner
                                    //.copyIExamStudentRoom2InfoExamStudentRoom(examStudentRoom);
                            return InfoExamStudentRoomWithInfoStudentAndInfoRoom.newInfoFromDomain(examStudentRoom);
                            
                        }
                    });

            List infoStudents = new ArrayList();
            Iterator iter = examStudentRoomList.iterator();
            while (iter.hasNext()) {
                IStudent student = ((IExamStudentRoom) iter.next())
                        .getStudent();
                //CLONER
                //InfoStudent infoStudent = Cloner
                        //.copyIStudent2InfoStudent(student);
                infoStudents.add(InfoStudent.newInfoFromDomain(student));
            }
            //CLONER
            //InfoExam infoExam = Cloner.copyIExam2InfoExam(exam);
            InfoExam infoExam = InfoExam.newInfoFromDomain(exam);
            
            ISiteComponent component = new InfoSiteTeacherStudentsEnrolledList(
                    infoStudents, infoExam, infoExamStudentRoomList);

            TeacherAdministrationSiteComponentBuilder componentBuilder = TeacherAdministrationSiteComponentBuilder
                    .getInstance();
            ISiteComponent commonComponent = componentBuilder.getComponent(
                    new InfoSiteCommon(), site, null, null, null);

            TeacherAdministrationSiteView siteView = new TeacherAdministrationSiteView(
                    commonComponent, component);
            return siteView;
        } catch (ExcepcaoPersistencia e) {
            throw new FenixServiceException(e);
        }
    }
}