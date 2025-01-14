/*-
 * #%L
 * Taggable Text Area
 * %%
 * Copyright (C) 2024 Vaadin Ltd
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
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A custom field component that combines a {@link TextField} with a {@link ListBox}.
 * 
 * <p>The {@code FilterListBoxSelector} allows to filter items in the list based on text input
 * and select an item either by pressing Enter or interacting with the {@link ListBox}.
 * 
 * <p>For a large amount of items use {@link FilterListSelector}.
 *
 * @param <T> the type of items displayed in the {@link ListBox}
 */
@SuppressWarnings("serial")
@CssImport("./styles/filter-listbox-selector.css")
public class FilterListBoxSelector<T> extends BaseFilterListSelector<T> {
	
	ListBox<T> listBox = new ListBox<>();
	
    /**
     * Constructs a {@code FilterListBoxSelector} with the given list of items.
     *
     * @param items the list of items to display in the {@link ListBox}
     */
	public FilterListBoxSelector(List<T> items) {
	    setClassName("taggable-textarea-filter-listbox-selector");
		listBox.setItems(items);
		listBox.getElement().getClassList().set("taggable-textarea-filter-listbox-selector-listbox", true);
		listBox.setSizeFull();
		initFilter(items);
		TextField filter = getFilter();
		filter.addValueChangeListener(e -> {
		    List<T> filteredItems = getFilteredItems();
			filteredItems = items.stream()
					.filter(item -> getFilterExpression().apply(item, filter.getValue()))
					.collect(Collectors.toList());
			listBox.setItems(filteredItems);
		});
		filter.getElement().executeJs("this.addEventListener('keydown', (event) => {"
				+ "if (event.key === \"ArrowDown\") {\n"
				+ "  $0.focus({ preventScroll: true });\n"
				+ " }\n"
				+ "});",listBox.getElement());
		listBox.addValueChangeListener(ev->{
			if (ev.isFromClient()) {
    			this.setValue(listBox.getValue());
			}
		});
		VerticalLayout layout = new VerticalLayout(filter,listBox);
		layout.setSpacing(false);
		layout.setPadding(false);
		layout.setMargin(false);
		layout.setWidth("auto");
		layout.setHeight("auto");
		add(layout);
	}
	
    /**
     * Sets a custom renderer for displaying items in the {@link ListBox}.
     *
     * @param itemRenderer the renderer to use for items
     */
    public void setRenderer(
            ComponentRenderer<? extends Component, T> itemRenderer) {
        this.listBox.setRenderer(itemRenderer);
    }
	
	@Override
	public T getValue() {
		return listBox.getValue();
	}

	@Override
	protected T generateModelValue() {
		return listBox.getValue();
	}

	@Override
	protected void setPresentationValue(T newPresentationValue) {
		listBox.setValue(newPresentationValue);
	}
}