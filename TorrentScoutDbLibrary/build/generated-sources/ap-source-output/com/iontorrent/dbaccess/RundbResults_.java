package com.iontorrent.dbaccess;

import com.iontorrent.dbaccess.RundbAnalysismetrics;
import com.iontorrent.dbaccess.RundbExperiment;
import com.iontorrent.dbaccess.RundbLibmetrics;
import com.iontorrent.dbaccess.RundbQualitymetrics;
import com.iontorrent.dbaccess.RundbTfmetrics;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.2.0.v20110202-r8913", date="2012-01-06T12:12:21")
@StaticMetamodel(RundbResults.class)
public class RundbResults_ { 

    public static volatile CollectionAttribute<RundbResults, RundbQualitymetrics> rundbQualitymetricsCollection;
    public static volatile SingularAttribute<RundbResults, String> fastqLink;
    public static volatile SingularAttribute<RundbResults, RundbExperiment> rundbExperiment;
    public static volatile SingularAttribute<RundbResults, Date> timeStamp;
    public static volatile SingularAttribute<RundbResults, String> status;
    public static volatile SingularAttribute<RundbResults, String> log;
    public static volatile SingularAttribute<RundbResults, String> timeToComplete;
    public static volatile SingularAttribute<RundbResults, Integer> id;
    public static volatile CollectionAttribute<RundbResults, RundbLibmetrics> rundbLibmetricsCollection;
    public static volatile SingularAttribute<RundbResults, String> sffLink;
    public static volatile SingularAttribute<RundbResults, String> tfFastq;
    public static volatile SingularAttribute<RundbResults, Integer> processedCycles;
    public static volatile SingularAttribute<RundbResults, String> reportLink;
    public static volatile SingularAttribute<RundbResults, String> tfSffLink;
    public static volatile SingularAttribute<RundbResults, Integer> framesProcessed;
    public static volatile CollectionAttribute<RundbResults, RundbTfmetrics> rundbTfmetricsCollection;
    public static volatile CollectionAttribute<RundbResults, RundbAnalysismetrics> rundbAnalysismetricsCollection;
    public static volatile SingularAttribute<RundbResults, String> resultsName;

}