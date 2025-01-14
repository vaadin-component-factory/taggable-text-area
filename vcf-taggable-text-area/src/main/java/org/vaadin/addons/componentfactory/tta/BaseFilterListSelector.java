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

import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableBiFunction;
import java.util.List;

@SuppressWarnings("serial")
public abstract class BaseFilterListSelector<T> extends CustomField<T>
    implements SingleSelect<CustomField<T>, T>, HasStyle {

  private final TextField filter = new TextField();

  /**
   * Function defining the filter expression, used to match items against the filter query. Defaults
   * to checking if the string representation of the item contains the filter text.
   */
  private SerializableBiFunction<T, String, Boolean> filterExpression =
      (item, filter) -> filter.contains("" + item);

  private List<T> filteredItems;

  protected void initFilter(List<T> items) {
    filter.getElement().executeJs("return;")
        .then(ev -> filter.getElement().executeJs("this.focus();"));
    filter.setSizeFull();
    filter.setValueChangeMode(ValueChangeMode.EAGER);
    filteredItems = items;
    filter.addKeyPressListener(Key.ENTER, ev -> {
      if (!filteredItems.isEmpty()) {
        this.setValue(filteredItems.iterator().next());
      }
    });
  }

  /**
   * Returns the filter expression used to filter items.
   *
   * @return the filter expression
   */
  public SerializableBiFunction<T, String, Boolean> getFilterExpression() {
    return filterExpression;
  }

  /**
   * Sets the filter expression used to filter items.
   *
   * @param filterExpression the filter expression
   */
  public void setFilterExpression(SerializableBiFunction<T, String, Boolean> filterExpression) {
    this.filterExpression = filterExpression;
  }

  /**
   * Returns the filter text field.
   * 
   * @return the filter
   */
  protected TextField getFilter() {
    return filter;
  }

  /**
   * Returns the filtered items list.
   * 
   * @return the filteredItems
   */
  protected List<T> getFilteredItems() {
    return filteredItems;
  }
  
}
