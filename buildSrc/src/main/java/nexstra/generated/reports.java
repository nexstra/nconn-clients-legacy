package nexstra.generated;;


public class reports {

  private long report_id;
  private String name;
  private String description;
  public enum  report_typeEnum { SQL, XML };

  private report_typeEnum report_type;
  private String dbname;
  private String sql_query;

 /* report_id : long
[name:report_id, type:long, annos:, setter:] */
  public long getReport_id() {
    return report_id;
  }

  public void setReport_id(long report_id) { 
    this.report_id = report_id;
  }

 /* name : String
[name:name, type:String, annos:, setter:] */
  public String getName() {
    return name;
  }

  public void setName(String name) { 
    this.name = name;
  }

 /* description : String
[name:description, type:String, annos:, setter:] */
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) { 
    this.description = description;
  }

 /* report_type : report_typeEnum
[name:report_type, type:report_typeEnum, annos:enum  report_typeEnum { SQL, XML };
, setter:( String value) { report_type = report_typeEnum.valueOf(value); }] */
  public report_typeEnum getReport_type() {
    return report_type;
  }

  public void setReport_type(report_typeEnum report_type) { 
    this.report_type = report_type;
  }
  public void setReport_type( String value) { report_type = report_typeEnum.valueOf(value); }

 /* dbname : String
[name:dbname, type:String, annos:, setter:] */
  public String getDbname() {
    return dbname;
  }

  public void setDbname(String dbname) { 
    this.dbname = dbname;
  }

 /* sql_query : String
[name:sql_query, type:String, annos:, setter:] */
  public String getSql_query() {
    return sql_query;
  }

  public void setSql_query(String sql_query) { 
    this.sql_query = sql_query;
  }

}
