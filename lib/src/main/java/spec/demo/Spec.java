package spec.demo;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@Data
@Builder
public class Spec {
  
  private String attrLsc;
  
  private String attrLid;
  
  private String attrValue;
  
  private String parameterLsc;
  
  private String parameterLid;
  
  private String parameterValue;
  
  public boolean isSameAttr(Spec spec) {
    return spec != null && Objects.equals(spec.attrLsc, this.attrLsc) && Objects.equals(spec.attrLid, this.attrLid);
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Spec spec = (Spec) o;
    return Objects.equals(parameterLsc, spec.parameterLsc) && Objects.equals(parameterLid, spec.parameterLid);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(parameterLsc, parameterLid);
  }
}
