package com.iontorrent.dbaccess;

import com.iontorrent.dbaccess.RundbResults;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.2.0.v20110202-r8913", date="2012-01-06T12:12:21")
@StaticMetamodel(RundbExperiment.class)
public class RundbExperiment_ { 

    public static volatile SingularAttribute<RundbExperiment, Integer> cycles;
    public static volatile SingularAttribute<RundbExperiment, String> chipType;
    public static volatile SingularAttribute<RundbExperiment, Boolean> usePreBeadfind;
    public static volatile SingularAttribute<RundbExperiment, Boolean> autoAnalyze;
    public static volatile SingularAttribute<RundbExperiment, String> libraryKey;
    public static volatile SingularAttribute<RundbExperiment, Date> date;
    public static volatile SingularAttribute<RundbExperiment, String> reagentBarcode;
    public static volatile SingularAttribute<RundbExperiment, Integer> id;
    public static volatile CollectionAttribute<RundbExperiment, RundbResults> rundbResultsCollection;
    public static volatile SingularAttribute<RundbExperiment, String> expCompInfo;
    public static volatile SingularAttribute<RundbExperiment, String> expDir;
    public static volatile SingularAttribute<RundbExperiment, String> storageOptions;
    public static volatile SingularAttribute<RundbExperiment, String> seqKitBarcode;
    public static volatile SingularAttribute<RundbExperiment, String> sample;
    public static volatile SingularAttribute<RundbExperiment, String> flowsInOrder;
    public static volatile SingularAttribute<RundbExperiment, String> expName;
    public static volatile SingularAttribute<RundbExperiment, String> pgmName;
    public static volatile SingularAttribute<RundbExperiment, Boolean> star;
    public static volatile SingularAttribute<RundbExperiment, String> storageHost;
    public static volatile SingularAttribute<RundbExperiment, Boolean> baselineRun;
    public static volatile SingularAttribute<RundbExperiment, String> log;
    public static volatile SingularAttribute<RundbExperiment, Integer> flows;
    public static volatile SingularAttribute<RundbExperiment, String> ftpStatus;
    public static volatile SingularAttribute<RundbExperiment, String> project;
    public static volatile SingularAttribute<RundbExperiment, String> chipBarcode;
    public static volatile SingularAttribute<RundbExperiment, String> library;
    public static volatile SingularAttribute<RundbExperiment, String> notes;

}