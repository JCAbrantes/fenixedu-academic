/*
 * Created on 13/Mar/2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package ServidorAplicacao.Servico.gesdis.teacher;

import DataBeans.gesdis.InfoBibliographicReference;
import Dominio.IBibliographicReference;
import Dominio.IDisciplinaExecucao;
import ServidorAplicacao.FenixServiceException;
import ServidorAplicacao.IServico;
import ServidorPersistente.ExcepcaoPersistencia;
import ServidorPersistente.IPersistentBibliographicReference;
import ServidorPersistente.ISuportePersistente;
import ServidorPersistente.OJB.SuportePersistenteOJB;
import ServidorPersistente.exceptions.ExistingPersistentException;

/**
 * @author PTRLV
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EditBibliographicReference implements IServico {	   

	private static EditBibliographicReference service = new EditBibliographicReference();    
	public static EditBibliographicReference getService() {
		return service;
	}
    
	private EditBibliographicReference() { }    
	public final String getNome() {
		return "Techer.EditBibliographicReference";
	}    

	private IBibliographicReference validateData(IDisciplinaExecucao executionCourse,InfoBibliographicReference bibliographicReferenceOld, String title, String authors, String reference, String year)
	throws ExistingPersistentException,ExcepcaoPersistencia {        
		IBibliographicReference bibliographicReferenceRead = null;        
		ISuportePersistente sp = SuportePersistenteOJB.getInstance();
		IPersistentBibliographicReference persistentBibliographicReference = null;
		persistentBibliographicReference = sp.getIPersistentBibliographicReference();        
		if (!(bibliographicReferenceOld.getTitle().equals(title) &&
			bibliographicReferenceOld.getAuthors().equals(authors) &&
			bibliographicReferenceOld.getReference().equals(reference) &&
			bibliographicReferenceOld.getYear() == year))
		 {            
			bibliographicReferenceRead = persistentBibliographicReference.readBibliographicReference(executionCourse,title,
																			authors,
																			reference,
																			year);            
			if (bibliographicReferenceRead != null){}
				throw new ExistingPersistentException();
		}        
		bibliographicReferenceRead = persistentBibliographicReference.readBibliographicReference(executionCourse,
																		bibliographicReferenceOld.getTitle(),
																		bibliographicReferenceOld.getAuthors(),
																		bibliographicReferenceOld.getReference(),
																		bibliographicReferenceOld.getYear()
																		);
		if (bibliographicReferenceRead == null)
			throw new ExcepcaoPersistencia();        
		return bibliographicReferenceRead;        
	}
    
	public void run(IDisciplinaExecucao executionCourse,
					InfoBibliographicReference bibliographicReferenceOld,
					String title,
					String authors,
					String reference,
					String year,
					Boolean optional)
	throws FenixServiceException {
		try {       		
		ISuportePersistente sp = SuportePersistenteOJB.getInstance();       
		IPersistentBibliographicReference persistentBibliographicReference= null;
		persistentBibliographicReference = sp.getIPersistentBibliographicReference();        
		IBibliographicReference bibliographicReference= null;        
		bibliographicReference = validateData(executionCourse,bibliographicReferenceOld,title,authors,reference,year);        
		bibliographicReference.setTitle(title);
		bibliographicReference.setAuthors(authors);
		bibliographicReference.setReference(reference);
		bibliographicReference.setYear(year);
		bibliographicReference.setOptional(optional);
		persistentBibliographicReference.lockWrite(bibliographicReference);
		}
		catch (ExcepcaoPersistencia e) {
			throw new FenixServiceException(e);
		}	
	}	
}
