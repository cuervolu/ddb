package dev.cuervolu.ddb.backend.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Set;

@JsonInclude(Include.NON_EMPTY)
public class ExceptionResponse {

  private Integer code;
  private String description;
  private String error;
  @JsonProperty("validation_errors")
  private Set<String> validationErrors;
  private Map<String, String> errors;

  public ExceptionResponse() {
  }

  private ExceptionResponse(ExceptionResponseBuilder builder) {
    this.code = builder.code;
    this.description = builder.description;
    this.error = builder.error;
    this.validationErrors = builder.validationErrors;
    this.errors = builder.errors;
  }

  public Integer getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  public String getError() {
    return error;
  }

  public Set<String> getValidationErrors() {
    return validationErrors;
  }

  public Map<String, String> getErrors() {
    return errors;
  }

  public static class ExceptionResponseBuilder {

    private Integer code;
    private String description;
    private String error;
    private Set<String> validationErrors;
    private Map<String, String> errors;

    public ExceptionResponseBuilder code(Integer code) {
      this.code = code;
      return this;
    }

    public ExceptionResponseBuilder description(String description) {
      this.description = description;
      return this;
    }

    public ExceptionResponseBuilder error(String error) {
      this.error = error;
      return this;
    }

    public ExceptionResponseBuilder validationErrors(Set<String> validationErrors) {
      this.validationErrors = validationErrors;
      return this;
    }

    public ExceptionResponseBuilder errors(Map<String, String> errors) {
      this.errors = errors;
      return this;
    }

    public ExceptionResponse build() {
      return new ExceptionResponse(this);
    }
  }
}