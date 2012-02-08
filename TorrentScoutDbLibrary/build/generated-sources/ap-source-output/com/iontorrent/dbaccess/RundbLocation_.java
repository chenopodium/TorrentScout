package com.iontorrent.dbaccess;

import com.iontorrent.dbaccess.RundbRig;
import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.2.0.v20110202-r8913", date="2012-01-06T12:12:21")
@StaticMetamodel(RundbLocation.class)
public class RundbLocation_ { 

    public static volatile SingularAttribute<RundbLocation, Integer> id;
    public static volatile CollectionAttribute<RundbLocation, RundbRig> rundbRigCollection;
    public static volatile SingularAttribute<RundbLocation, String> name;
    public static volatile SingularAttribute<RundbLocation, String> comments;

}