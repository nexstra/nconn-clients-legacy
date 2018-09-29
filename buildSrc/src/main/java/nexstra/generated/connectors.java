package nexstra.generated;


public class connectors {

  private long connector_id;
  private long connector_type_id;
  private String name;
  private long client_id;
  private long running;
  private String state;
  private String run_type;
  private java.sql.Timestamp run_start;
  private long run_every;
  private String run_every_unit;
  private String properties;
  private java.sql.Timestamp start_dt;
  private java.sql.Timestamp end_dt;
  private String log_data;
  private String config_data;


  public long getConnector_id() {
    return connector_id;
  }

  public void setConnector_id(long connector_id) {
    this.connector_id = connector_id;
  }


  public long getConnector_type_id() {
    return connector_type_id;
  }

  public void setConnector_type_id(long connector_type_id) {
    this.connector_type_id = connector_type_id;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public long getClient_id() {
    return client_id;
  }

  public void setClient_id(long client_id) {
    this.client_id = client_id;
  }


  public long getRunning() {
    return running;
  }

  public void setRunning(long running) {
    this.running = running;
  }


  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }


  public String getRun_type() {
    return run_type;
  }

  public void setRun_type(String run_type) {
    this.run_type = run_type;
  }


  public java.sql.Timestamp getRun_start() {
    return run_start;
  }

  public void setRun_start(java.sql.Timestamp run_start) {
    this.run_start = run_start;
  }


  public long getRun_every() {
    return run_every;
  }

  public void setRun_every(long run_every) {
    this.run_every = run_every;
  }


  public String getRun_every_unit() {
    return run_every_unit;
  }

  public void setRun_every_unit(String run_every_unit) {
    this.run_every_unit = run_every_unit;
  }


  public String getProperties() {
    return properties;
  }

  public void setProperties(String properties) {
    this.properties = properties;
  }


  public java.sql.Timestamp getStart_dt() {
    return start_dt;
  }

  public void setStart_dt(java.sql.Timestamp start_dt) {
    this.start_dt = start_dt;
  }


  public java.sql.Timestamp getEnd_dt() {
    return end_dt;
  }

  public void setEnd_dt(java.sql.Timestamp end_dt) {
    this.end_dt = end_dt;
  }


  public String getLog_data() {
    return log_data;
  }

  public void setLog_data(String log_data) {
    this.log_data = log_data;
  }


  public String getConfig_data() {
    return config_data;
  }

  public void setConfig_data(String config_data) {
    this.config_data = config_data;
  }

}
