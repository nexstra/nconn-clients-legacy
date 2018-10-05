package nexstra.generated;;


public class reports_input {

  private long report_input_id;
  private long report_id;
  private long col;
  private String name;
  private String description;
  public enum  typeEnum { INTEGER, STRING, DATE, DOUBLE };

  private typeEnum type;
  private long fillin;
  private String def_value;

 /* report_input_id : long
[name:report_input_id, type:long, annos:, setter:] */
  public long getReport_input_id() {
    return report_input_id;
  }

  public void setReport_input_id(long report_input_id) { 
    this.report_input_id = report_input_id;
  }

 /* report_id : long
[name:report_id, type:long, annos:, setter:] */
  public long getReport_id() {
    return report_id;
  }

  public void setReport_id(long report_id) { 
    this.report_id = report_id;
  }

 /* col : long
[name:col, type:long, annos:, setter:] */
  public long getCol() {
    return col;
  }

  public void setCol(long col) { 
    this.col = col;
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

 /* type : typeEnum
[name:type, type:typeEnum, annos:enum  typeEnum { INTEGER, STRING, DATE, DOUBLE };
, setter:( String value) { type = typeEnum.valueOf(value); }] */
  public typeEnum getType() {
    return type;
  }

  public void setType(typeEnum type) { 
    this.type = type;
  }
  public void setType( String value) { type = typeEnum.valueOf(value); }

 /* fillin : long
[name:fillin, type:long, annos:, setter:] */
  public long getFillin() {
    return fillin;
  }

  public void setFillin(long fillin) { 
    this.fillin = fillin;
  }

 /* def_value : String
[name:def_value, type:String, annos:, setter:] */
  public String getDef_value() {
    return def_value;
  }

  public void setDef_value(String def_value) { 
    this.def_value = def_value;
  }

}
