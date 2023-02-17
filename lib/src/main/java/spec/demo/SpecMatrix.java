package spec.demo;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SpecMatrix {
  
  // 规格连通图
  private final Map<Spec, MatrixItem> matrix = new HashMap<>();
  
  // 所有的Sku
  private List<MaskedSku> skus;
  
  // 所有选中的规格
  private final Set<Spec> selected = new HashSet<>();
  
  // 选中状态初始默认值
  private int defaultSelectedMask = 0;
  
  // 缓存选中的规格结果
  private int selectedMask;
  
  public SpecMatrix(List<Spec> specs, List<Sku> skus) {
    // 生成连通图
    for (int i = 0; i < specs.size(); i++) {
      this.matrix.put(specs.get(i), new MatrixItem(i));
    }
    // 构造连通图
    buildSpecMatrix(specs, skus);
  }
  
  public SpecMatrix resetSkus(List<Sku> skus) {
    // 重置连通图
    this.matrix.values().forEach(MatrixItem::resetMask);
    // 重新构造连通图
    buildSpecMatrix(this.matrix.keySet(), skus);
    return this;
  }
  
  private void buildSpecMatrix(Collection<Spec> specs, List<Sku> skus) {
    // 相同属性之间默认能够互通
    groupedByAttr(specs).forEach(this::contactWithOthers);
    // Sku规格连通图
    skus.forEach(sku -> contactWithOthers(sku.getSpecs()));
    // 构造Sku Mask
    this.skus = skus.stream().map(MaskedSku::new).toList();
    initSelectedMask();
  }
  
  private void initSelectedMask() {
    int mask = 0;
    for (Spec spec : matrix.keySet()) {
      mask |= matrix.get(spec).getMask();
    }
    this.selectedMask = this.defaultSelectedMask = mask;
  }
  
  private static List<List<Spec>> groupedByAttr(Collection<Spec> specs) {
    return specs.stream().collect(Collectors.groupingBy(spec -> new AttrKey(spec.getAttrLsc(), spec.getAttrLid())))
        .values().stream().toList();
  }
  
  private void contactWithOthers(List<Spec> specList) {
    for (Spec row : specList) {
      for (Spec col : specList) {
        if (!row.equals(col)) {
          matrix.get(row).contactWith(matrix.get(col));
        }
      }
    }
  }
  
  public SpecMatrix selected(Collection<Spec> selected) {
    if (groupedByAttr(selected).stream().anyMatch(specs -> specs.size() > 1)) {
      throw new RuntimeException("传入的规格存在相同属性");
    }
    this.clear();
    this.selected.addAll(selected);
    resetSelectedMask();
    return this;
  }
  
  public SpecMatrix selected(Spec... selected) {
    return selected(Set.of(selected));
  }
  
  public SpecMatrix addSelected(Spec selected) {
    if (this.selected.stream().anyMatch(spec -> spec.equals(selected))) {
      return this;
    }
    // 同属性点击移除
    this.selected.stream().filter(spec -> spec.isSameAttr(selected)).findFirst().ifPresent(this.selected::remove);
    this.selected.add(selected);
    resetSelectedMask();
    return this;
  }
  
  public SpecMatrix remove(Spec removed) {
    this.selected.remove(removed);
    resetSelectedMask();
    return this;
  }
  
  /**
   * 清空所有选中规格
   *
   * @return 当前规格连通图
   */
  public SpecMatrix clear() {
    this.selected.clear();
    this.selectedMask = this.defaultSelectedMask;
    return this;
  }
  
  /**
   * 获取当前选中状态下有效的Sku
   *
   * @return 有效的Sku
   */
  public List<Sku> validSku() {
    return this.skus.stream().filter(sku -> (sku.getMask() & selectedMask) > 0).map(MaskedSku::getSku).toList();
  }
  
  /**
   * 获取当前选中状态下有效的规格
   *
   * @return
   */
  public List<Spec> validSpec() {
    List<Spec> specs = new ArrayList<>();
    matrix.forEach((spec, item) -> {
      if ((item.getIndexNumber() & selectedMask) > 0) {
        specs.add(spec);
      }
    });
    return specs;
  }
  
  /**
   * 重置计算选中的规格掩码
   */
  private void resetSelectedMask() {
    int mask = defaultSelectedMask;
    for (Spec spec : selected) {
      mask &= matrix.get(spec).getMask();
    }
    selectedMask = mask;
  }
  
  @Getter
  private class MaskedSku {
    
    private final Sku sku;
    
    private final long mask;
    
    public MaskedSku(Sku sku) {
      this.sku = sku;
      
      // 计算Sku mask
      long mask = 0;
      for (Spec spec : sku.getSpecs()) {
        mask |= matrix.get(spec).getIndexNumber();
      }
      this.mask = mask;
    }
  }
  
  @Getter
  private static class MatrixItem {
    
    private final int indexNumber;
    
    private long mask;
    
    public MatrixItem(int idx) {
      this.indexNumber = 1 << idx;
    }
    
    public void contactWith(MatrixItem other) {
      this.mask |= other.getIndexNumber();
      other.mask |= this.getIndexNumber();
    }
    
    private long getIndexNumber() {
      return this.indexNumber;
    }
    
    public void resetMask() {
      this.mask = 0;
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
