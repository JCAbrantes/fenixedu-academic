package net.sourceforge.fenixedu.applicationTier.Servico.research.result;

import net.sourceforge.fenixedu.accessControl.AccessControl;
import net.sourceforge.fenixedu.applicationTier.Service;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.FenixServiceException;
import net.sourceforge.fenixedu.domain.research.result.Result;
import net.sourceforge.fenixedu.domain.research.result.ResultParticipation;
import net.sourceforge.fenixedu.persistenceTier.ExcepcaoPersistencia;

public class DeleteResultParticipation extends Service {
    public void run(ResultParticipation participation) throws ExcepcaoPersistencia, FenixServiceException {
        final Result result = participation.getResult();
        participation.delete();  
        result.setModificationDateAndAuthor(AccessControl.getUserView().getPerson().getName());
    }
}
