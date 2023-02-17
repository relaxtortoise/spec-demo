package spec.demo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Builder
@Data
public class Sku {
  
  private Integer id;
  
  private String name;
  
  private String picture;
  
  private Integer stock;
  
  private List<Spec> specs;
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Sku sku = (Sku) o;
    return Objects.equals(id, sku.id);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
