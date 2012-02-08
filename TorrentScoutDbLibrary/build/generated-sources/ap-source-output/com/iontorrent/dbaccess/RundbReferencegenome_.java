package com.iontorrent.dbaccess;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.2.0.v20110202-r8913", date="2012-01-06T12:12:21")
@StaticMetamodel(RundbReferencegenome.class)
public class RundbReferencegenome_ { 

    public static volatile SingularAttribute<RundbReferencegenome, Integer> id;
    public static volatile SingularAttribute<RundbReferencegenome, Boolean> enabled;
    public static volatile SingularAttribute<RundbReferencegenome, String> species;
    public static volatile SingularAttribute<RundbReferencegenome, String> source;
    public static volatile SingularAttribute<RundbReferencegenome, String> status;
    public static volatile SingularAttribute<RundbReferencegenome, String> name;
    public static volatile SingularAttribute<RundbReferencegenome, String> referencePath;
    public static volatile SingularAttribute<RundbReferencegenome, Date> date;
    public static volatile SingularAttribute<RundbReferencegenome, String> notes;
    public static volatile SingularAttribute<RundbReferencegenome, String> prettyName;
    public static volatile SingularAttribute<RundbReferencegenome, String> version;

}