/*-
 * #%L
 * Taggable Text Area
 * %%
 * Copyright (C) 2025 Vaadin Ltd
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.vaadin.addons.componentfactory.tta;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A custom field component that combines a {@link TextField} with a {@link Grid}.
 * 
 * <p>
 * The {@code FilterListSelector} allows to filter items in the list based on text input and select
 * an item either by pressing Enter or interacting with the {@link Grid}.
 *
 * This particular selector is advised to be used when a large amount of items needs to be
 * displayed.
 * 
 * @param <T> the type of items displayed
 */
@SuppressWarnings("serial")
@CssImport(value = "./styles/filter-list-selector.css", themeFor = "vaadin-grid")
public class FilterListSelector<T> extends BaseFilterListSelector<T> {

  private Grid<T> gridList = new Grid<>();
  private Column<T> column;
  private ComponentRenderer<? extends Component, T> itemRenderer;
  private T selectedItem = null;
  private T focusedItem = null;
  private int visibleItems = 10;

  /**
   * Constructs a {@code FilterListSelector} with the given list of items.
   *
   * @param items the list of items to display in the {@link Grid}
   */
  public FilterListSelector(List<T> items) {
    this(items, new ComponentRenderer<>(item -> {
      HorizontalLayout row = new HorizontalLayout();
      row.setAlignItems(FlexComponent.Alignment.CENTER);

      Span name = new Span("" + item);

      VerticalLayout column = new VerticalLayout(name);
      column.setPadding(false);
      column.setSpacing(false);

      row.add(column);
      row.getStyle().set("line-height", "var(--lumo-line-height-m)");
      return row;
    }));
  }

  /**
   * Constructs a {@code FilterListSelector} with the given list of items and the given
   * itemRenderer.
   *
   * @param items the list of items to display in the {@link Grid}
   * @param itemRenderer the renderer for the items
   */
  public FilterListSelector(List<T> items, ComponentRenderer<? extends Component, T> itemRenderer) {
    setClassName("taggable-textarea-filter-list-selector");
    this.itemRenderer = itemRenderer;

    gridList.addClassName("taggable-textarea-filter-list-selector-list");
    gridList.setSelectionMode(SelectionMode.SINGLE);
    gridList.setItems(items);
    gridList.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_NO_BORDER);
    gridList.setAllRowsVisible(items.size() < visibleItems);
    column = gridList.addColumn(this.itemRenderer);
    column.setClassNameGenerator(c -> "taggable-textarea-filter-list-selector-list-column");

    initFilter(items);
    TextField filter = getFilter();

    filter.addValueChangeListener(e -> {
      List<T> filteredItems = getFilteredItems();
      filteredItems =
          items.stream().filter(item -> getFilterExpression().apply(item, filter.getValue()))
              .collect(Collectors.toList());
      gridList.setItems(filteredItems);
      gridList.setAllRowsVisible(filteredItems.size() < visibleItems);
    });
    filter.getElement().executeJs(
        "this.addEventListener('keydown', (event) => {" +
        "    if (event.key === 'ArrowDown') { " +
        "        const grid = $0;" +
        "        requestAnimationFrame(() => { " +
        "            const firstCell = grid.shadowRoot.querySelector('.taggable-textarea-filter-list-selector-list-column');" +
        "            if (firstCell) {" +
        "                firstCell.focus({ preventScroll: true });" + 
        "            }" +
        "        });" +
        "    }" +
        "});",
        gridList.getElement()
    );

    // add selection listener to gridList to save the selected item
    gridList.addSelectionListener(ev -> {
      Optional<T> optionalSelectedItem = ev.getFirstSelectedItem();
      selectedItem = optionalSelectedItem.orElse(null);
      this.setValue(selectedItem);
    });

    // keep track of the focus item so it can be selected on enter
    gridList.addCellFocusListener(focusEvent -> {
      focusedItem = focusEvent.getItem().orElse(null);
    });

    // add shorcut listener so items can be selected on enter
    Shortcuts.addShortcutListener(this, event -> {
      gridList.select(focusedItem);
    }, Key.ENTER).listenOn(gridList);

    VerticalLayout layout = new VerticalLayout(filter, gridList);
    layout.setSpacing(false);
    layout.setPadding(false);
    layout.setMargin(false);
    layout.setWidth("auto");
    layout.setHeight("auto");
    add(layout);
  }
  
  @Override
  public T getValue() {
    return this.selectedItem;
  }
  
  @Override
  protected T generateModelValue() {
    return this.selectedItem;
  }

  @Override
  protected void setPresentationValue(T newPresentationValue) {
    gridList.select(newPresentationValue);
  }

  /**
   * @param visibleItems the visibleItems to set
   */
  public void setVisibleItems(int visibleItems) {
    this.visibleItems = visibleItems;
  }

}
