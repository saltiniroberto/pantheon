package tech.pegasys.pantheon.ethereum.jsonrpc.internal.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Collection;
import java.util.Optional;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

public class FilterRepositoryTest {

  private FilterRepository repository;

  @Before
  public void before() {
    repository = new FilterRepository();
  }

  @Test
  public void getFiltersShouldReturnAllFilters() {
    BlockFilter filter1 = new BlockFilter("foo");
    BlockFilter filter2 = new BlockFilter("bar");
    repository.save(filter1);
    repository.save(filter2);

    Collection<Filter> filters = repository.getFilters();

    assertThat(filters).containsExactlyInAnyOrderElementsOf(Lists.newArrayList(filter1, filter2));
  }

  @Test
  public void getFiltersShouldReturnEmptyListWhenRepositoryIsEmpty() {
    assertThat(repository.getFilters()).isEmpty();
  }

  @Test
  public void saveShouldAddFilterToRepository() {
    BlockFilter filter = new BlockFilter("id");
    repository.save(filter);

    BlockFilter retrievedFilter = repository.getFilter("id", BlockFilter.class).get();

    assertThat(retrievedFilter).isEqualToComparingFieldByField(filter);
  }

  @Test
  public void saveNullFilterShouldFail() {
    Throwable throwable = catchThrowable(() -> repository.save(null));

    assertThat(throwable)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Can't save null filter");
  }

  @Test
  public void saveFilterWithSameIdShouldFail() {
    BlockFilter filter = new BlockFilter("x");
    repository.save(filter);

    Throwable throwable = catchThrowable(() -> repository.save(filter));

    assertThat(throwable)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Filter with id x already exists");
  }

  @Test
  public void getSingleFilterShouldReturnExistingFilterOfCorrectType() {
    BlockFilter filter = new BlockFilter("id");
    repository.save(filter);

    Optional<BlockFilter> optional = repository.getFilter(filter.getId(), BlockFilter.class);

    assertThat(optional.isPresent()).isTrue();
    assertThat(optional.get()).isEqualToComparingFieldByField(filter);
  }

  @Test
  public void getSingleFilterShouldReturnEmptyForFilterOfIncorrectType() {
    BlockFilter filter = new BlockFilter("id");
    repository.save(filter);

    Optional<PendingTransactionFilter> optional =
        repository.getFilter(filter.getId(), PendingTransactionFilter.class);

    assertThat(optional.isPresent()).isFalse();
  }

  @Test
  public void getSingleFilterShouldReturnEmptyForAbsentId() {
    BlockFilter filter = new BlockFilter("foo");
    repository.save(filter);

    Optional<BlockFilter> optional = repository.getFilter("bar", BlockFilter.class);

    assertThat(optional.isPresent()).isFalse();
  }

  @Test
  public void getSingleFilterShouldReturnEmptyForEmptyRepository() {
    Optional<BlockFilter> optional = repository.getFilter("id", BlockFilter.class);

    assertThat(optional.isPresent()).isFalse();
  }

  @Test
  public void getFilterCollectionShouldReturnAllFiltersOfSpecificType() {
    BlockFilter blockFilter1 = new BlockFilter("foo");
    BlockFilter blockFilter2 = new BlockFilter("biz");
    PendingTransactionFilter pendingTxFilter1 = new PendingTransactionFilter("bar");

    Collection<BlockFilter> expectedFilters = Lists.newArrayList(blockFilter1, blockFilter2);

    repository.save(blockFilter1);
    repository.save(blockFilter2);
    repository.save(pendingTxFilter1);

    Collection<BlockFilter> blockFilters = repository.getFiltersOfType(BlockFilter.class);

    assertThat(blockFilters).containsExactlyInAnyOrderElementsOf(expectedFilters);
  }

  @Test
  public void getFilterCollectionShouldReturnEmptyForNoneMatchingTypes() {
    PendingTransactionFilter filter = new PendingTransactionFilter("foo");
    repository.save(filter);

    Collection<BlockFilter> filters = repository.getFiltersOfType(BlockFilter.class);

    assertThat(filters).isEmpty();
  }

  @Test
  public void getFilterCollectionShouldReturnEmptyListForEmptyRepository() {
    Collection<BlockFilter> filters = repository.getFiltersOfType(BlockFilter.class);

    assertThat(filters).isEmpty();
  }

  @Test
  public void existsShouldReturnTrueForExistingId() {
    BlockFilter filter = new BlockFilter("id");
    repository.save(filter);

    assertThat(repository.exists("id")).isTrue();
  }

  @Test
  public void existsShouldReturnFalseForAbsentId() {
    BlockFilter filter = new BlockFilter("foo");
    repository.save(filter);

    assertThat(repository.exists("bar")).isFalse();
  }

  @Test
  public void existsShouldReturnFalseForEmptyRepository() {
    assertThat(repository.exists("id")).isFalse();
  }

  @Test
  public void deleteExistingFilterShouldDeleteSuccessfully() {
    BlockFilter filter = new BlockFilter("foo");
    repository.save(filter);
    repository.delete(filter.getId());

    assertThat(repository.exists(filter.getId())).isFalse();
  }

  @Test
  public void deleteAbsentFilterDoesNothing() {
    assertThat(repository.exists("foo")).isFalse();
    repository.delete("foo");
  }

  @Test
  public void deleteAllShouldClearFilters() {
    BlockFilter filter1 = new BlockFilter("foo");
    BlockFilter filter2 = new BlockFilter("biz");
    repository.save(filter1);
    repository.save(filter2);

    repository.deleteAll();

    assertThat(repository.exists(filter1.getId())).isFalse();
    assertThat(repository.exists(filter2.getId())).isFalse();
  }
}
