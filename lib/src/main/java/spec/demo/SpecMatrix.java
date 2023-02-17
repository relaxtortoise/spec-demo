package spec.demo;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SpecMatrix {
  
  private static final int DefaultMask = Integer.MAX_VALUE;
  
  // 规格连通图
  private final Map<Spec, MatrixItem> matrix = new HashMap<>();
  
  // 所有的Sku
  private final List<MaskedSku> skus;
  
  // 所有选中的规格
  private final Set<Spec> selected = new HashSet<>();
  
  // 缓存选中的规格结果
  private int selectedMask = DefaultMask;
  
  public SpecMatrix(List<Sku> skus, List<Spec> specs) {
    for (int i = 0; i < specs.size(); i++) {
      this.matrix.put(specs.get(i), new MatrixItem(i));
    }
    // 相同属性之间默认能够互通
    groupedByAttr(specs).forEach((attr, attrSpecs) -> contactWithOthers(attrSpecs));
    // 规格连通图
    skus.forEach(sku -> {
      contactWithOthers(sku.getSpecs());
    });
    // 构造
    this.skus = skus.stream().map(MaskedSku::new).toList();
  }
  
  private static Map<AttrKey, List<Spec>> groupedByAttr(List<Spec> specs) {
    return specs.stream().collect(Collectors.groupingBy(spec -> new AttrKey(spec.getAttrLsc(), spec.getAttrLid())));
  }
  
  private void contactWithOthers(List<Spec> specList) {
    for (Spec row : specList) {
      for (Spec col : specList) {
        matrix.get(row).contactWith(matrix.get(col));
      }
    }
  }
  
  public SpecMatrix selected(Spec selected) {
    this.selected.stream().filter(spec -> spec.isSameAttr(selected)).findFirst().ifPresent(this.selected::remove);
    this.selected.add(selected);
    resetSelectedMask();
    return this;
  }
  
  public SpecMatrix removeSelected(Spec removed) {
    this.selected.remove(removed);
    resetSelectedMask();
    return this;
  }
  
  public List<Sku> validSku() {
    return this.skus.stream().filter(sku -> (sku.getMask() & selectedMask) > 0).map(MaskedSku::getSku).toList();
  }
  
  /**
   * 获取所有
   *
   * @return
   */
  public List<Spec> validSpec() {
    List<Spec> specs = new ArrayList<>();
    matrix.forEach((spec, item) -> {
      if ((item.getMask() & selectedMask) > 0) {
        specs.add(spec);
      }
    });
    return specs;
  }
  
  /**
   * 重置计算选中的规格掩码
   */
  private void resetSelectedMask() {
    int mask = DefaultMask;
    for (Spec spec : selected) {
      mask &= matrix.get(spec).getMask();
    }
    selectedMask = mask;
  }
  
  @Getter
  private class MaskedSku {
    
    private final Sku sku;
    
    private final int mask;
    
    public MaskedSku(Sku sku) {
      this.sku = sku;
      
      // 计算Sku mask
      int mask = 0;
      for (Spec spec : sku.getSpecs()) {
        mask |= matrix.get(spec).getIndexNumber();
      }
      this.mask = mask;
    }
  }
  
  @Getter
  private static class MatrixItem {
    
    private final int indexNumber;
    
    private int mask;
    
    public MatrixItem(int idx) {
      this.indexNumber = 1 << idx;
    }
    
    public void contactWith(MatrixItem other) {
      this.mask |= other.getIndexNumber();
      other.mask |= this.getIndexNumber();
    }
    
    private int getIndexNumber() {
      return this.indexNumber;
    }
  }
  
  private record AttrKey(String lsc, String lid) {
    
    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      AttrKey attrKey = (AttrKey) o;
      return Objects.equals(lsc, attrKey.lsc) && Objects.equals(lid, attrKey.lid);
    }
    
  }
}
