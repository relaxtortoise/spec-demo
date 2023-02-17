package spec.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class MatrixTest {
  
  private static List<Spec> specs = List.of(
      Spec.builder().attrLsc("1").attrLid("1").attrValue("颜色").parameterLsc("1").parameterLid("1")
          .parameterValue("紫色").build(),
      Spec.builder().attrLsc("1").attrLid("1").attrValue("颜色").parameterLsc("1").parameterLid("2")
          .parameterValue("红色").build(),
      Spec.builder().attrLsc("1").attrLid("2").attrValue("运行内存").parameterLsc("1").parameterLid("3")
          .parameterValue("4G").build(),
      Spec.builder().attrLsc("1").attrLid("2").attrValue("运行内存").parameterLsc("1").parameterLid("4")
          .parameterValue("8G").build(),
      Spec.builder().attrLsc("1").attrLid("3").attrValue("存储内存").parameterLsc("1").parameterLid("5")
          .parameterValue("64G").build(),
      Spec.builder().attrLsc("1").attrLid("3").attrValue("存储内存").parameterLsc("1").parameterLid("6")
          .parameterValue("128G").build(),
      Spec.builder().attrLsc("1").attrLid("3").attrValue("存储内存").parameterLsc("1").parameterLid("7")
          .parameterValue("256G").build());
  
  private static List<Sku> skus = List.of(
      // 紫-4-64
      Sku.builder().id(1).name("规格1").specs(List.of(
          Spec.builder().attrLsc("1").attrLid("1").attrValue("颜色").parameterLsc("1").parameterLid("1")
              .parameterValue("紫色").build(),
          Spec.builder().attrLsc("1").attrLid("2").attrValue("运行内存").parameterLsc("1").parameterLid("3")
              .parameterValue("4G").build(),
          Spec.builder().attrLsc("1").attrLid("3").attrValue("存储内存").parameterLsc("1").parameterLid("5")
              .parameterValue("64G").build())).build(),
      // 紫-4-128
      Sku.builder().id(1).name("规格1").specs(List.of(
          Spec.builder().attrLsc("1").attrLid("1").attrValue("颜色").parameterLsc("1").parameterLid("1")
              .parameterValue("紫色").build(),
          Spec.builder().attrLsc("1").attrLid("2").attrValue("运行内存").parameterLsc("1").parameterLid("3")
              .parameterValue("4G").build(),
          Spec.builder().attrLsc("1").attrLid("3").attrValue("存储内存").parameterLsc("1").parameterLid("6")
              .parameterValue("128G").build())).build(),
      // 紫-8-128
      Sku.builder().id(1).name("规格1").specs(List.of(
          Spec.builder().attrLsc("1").attrLid("1").attrValue("颜色").parameterLsc("1").parameterLid("1")
              .parameterValue("紫色").build(),
          Spec.builder().attrLsc("1").attrLid("2").attrValue("运行内存").parameterLsc("1").parameterLid("4")
              .parameterValue("8G").build(),
          Spec.builder().attrLsc("1").attrLid("3").attrValue("存储内存").parameterLsc("1").parameterLid("6")
              .parameterValue("128G").build())).build(),
      // 红-8-256
      Sku.builder().id(1).name("规格1").specs(List.of(
          Spec.builder().attrLsc("1").attrLid("1").attrValue("颜色").parameterLsc("1").parameterLid("2")
              .parameterValue("红色").build(),
          Spec.builder().attrLsc("1").attrLid("2").attrValue("运行内存").parameterLsc("1").parameterLid("4")
              .parameterValue("8G").build(),
          Spec.builder().attrLsc("1").attrLid("3").attrValue("存储内存").parameterLsc("1").parameterLid("7")
              .parameterValue("256G").build())).build());
  
  private SpecMatrix matrix;
  
  @BeforeEach
  void setup() {
    this.matrix = new SpecMatrix(specs, skus);
  }
  
  @ParameterizedTest(name = "#{index} - 选中 {0} 期望 {1} 规格处于活跃状态")
  @MethodSource("spec_match_data")
  @DisplayName("选中部分规格，剩余的活跃规格")
  void should_valid_match_specs(List<Spec> selectedSpecs, List<Spec> expectedValidSpecs) {
    assertIterableEquals(expectedValidSpecs, matrix.selected(selectedSpecs).validSpec());
  }
  
  static Stream<Arguments> spec_match_data() {
    return Stream.of(
        // 选中 紫色
        arguments(getSpecs(0), getSpecs(1, 2, 3, 4, 5)),
        // 选中 红色
        arguments(getSpecs(1), getSpecs(0, 3, 6)),
        // 选中 4G
        arguments(getSpecs(2), getSpecs(0, 3, 4, 5)),
        // 选中 8G
        arguments(getSpecs(3), getSpecs(0, 1, 2, 5, 6)),
        // 选中 64G
        arguments(getSpecs(4), getSpecs(0, 2, 5, 6)),
        // 选中 128G
        arguments(getSpecs(5), getSpecs(0, 2, 3, 4, 6)),
        // 选中 256G
        arguments(getSpecs(6), getSpecs(1, 3, 4, 5)),
        // 选中 紫色-4G
        arguments(getSpecs(0, 2), getSpecs(3, 4, 5)),
        // 选中 紫色-8G
        arguments(getSpecs(0, 3), getSpecs(1, 2, 5)),
        // 选中 紫色-64G
        arguments(getSpecs(0, 4), getSpecs(2, 5)),
        // 选中 紫色-128G
        arguments(getSpecs(0, 5), getSpecs(2, 3, 4)),
        // 选中 紫色-256G
        arguments(getSpecs(0, 6), getSpecs(1, 3, 4, 5))
        // todo: 填充所有测试结果
    );
  }
  
  static Named<List<Spec>> getSpecs(int... index) {
    List<Spec> list = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    for (int i : index) {
      Spec spec = specs.get(i);
      list.add(spec);
      sb.append(spec.getParameterValue()).append(",");
    }
    return named(sb.deleteCharAt(sb.length() - 1).toString(), list);
  }
}
