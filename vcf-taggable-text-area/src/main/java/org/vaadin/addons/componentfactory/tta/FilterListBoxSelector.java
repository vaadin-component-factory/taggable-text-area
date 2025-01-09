package org.vaadin.addons.componentfactory.tta;

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableBiFunction;

@SuppressWarnings("serial")
@CssImport("./styles/filter-listbox-selector.css")
public class FilterListBoxSelector<T> extends CustomField<T> implements SingleSelect<CustomField<T>, T>, HasStyle {
	
	TextField filter = new TextField();
	ListBox<T> listBox = new ListBox<>();
	private SerializableBiFunction<T, String, Boolean> filterExpression = (item,filter) -> filter.contains(""+item);
	private List<T> filteredItems; 
	
	public FilterListBoxSelector(List<T> items) {
	    setClassName("taggable-textarea-filter-listbox-selector");
		listBox.setItems(items);
		filter.getElement().executeJs("return;").then(ev->filter.getElement().executeJs("this.focus();"));
		listBox.getElement().getClassList().set("taggable-textarea-filter-listbox-selector-listbox", true);
		listBox.setSizeFull();
		filter.setSizeFull();
		filter.setValueChangeMode(ValueChangeMode.EAGER);
		filteredItems = items;
		filter.addValueChangeListener(e -> {
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
		filter.addKeyPressListener(Key.ENTER, ev->{
			if (!filteredItems.isEmpty()) {
				this.setValue(filteredItems.iterator().next());
			}
		});
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

	public SerializableBiFunction<T, String, Boolean> getFilterExpression() {
		return filterExpression;
	}

	public void setFilterExpression(SerializableBiFunction<T, String, Boolean> filterExpression) {
		this.filterExpression = filterExpression;
	}
	
}