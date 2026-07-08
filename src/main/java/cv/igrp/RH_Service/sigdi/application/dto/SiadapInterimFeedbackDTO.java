package cv.igrp.RH_Service.sigdi.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiadapInterimFeedbackDTO {
    private String evaluationId;
    private String objectivesSynthesis;
    private String observedFactsStar;
    private String difficultiesObstacles;
    private String feedbackAndAction;
    private List<CompetencyObservationDTO> competencyObservations;
    private List<ImprovementActionDTO> improvementActions;
    private List<ObjectiveRevisionDTO> objectiveRevisions;
}
