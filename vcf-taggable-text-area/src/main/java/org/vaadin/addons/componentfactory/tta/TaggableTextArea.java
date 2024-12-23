/*
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.vaadin.addons.componentfactory.tta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jsoup.Jsoup;

import com.vaadin.componentfactory.Popup;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableFunction;

/**
 * Add-on that provides a modified text area that supports the usage of tags that can be selected from a popup
 *
 */
@SuppressWarnings("serial")
public class TaggableTextArea<T> extends TextArea {
	
	private Div content = new Div();
	protected List<T> items = new ArrayList<>();
	private SerializableFunction<T,String> labelGenerator = item->""+item;
	private SerializableFunction<T,Boolean> tagPopupVisibilityFunction = (item)->true;
	
	public TaggableTextArea(List<T> items) {
		content.getElement().setAttribute("contenteditable", true);
		content.getElement().setAttribute("slot", "textarea");
		content.getStyle().set("outline", "none");
		content.getStyle().set("overflow", "auto");
		content.setSizeFull();
		this.getElement().appendChild(content.getElement());
		content.getElement().executeJs("this.addEventListener(\"input\", () => {"
				+ "const content = this.innerHTML;"
				+ "this.parentNode.$server.updateContent(content);"
				+ "})");
		content.getElement().executeJs(
				"this.addEventListener(\"click\", (ev) => {"
				+ "if (ev && ev.srcElement && ev.srcElement.id && ev.srcElement.id.startsWith('span')) {"
				+ "this.parentNode.$server.showTagPopup(ev.srcElement.id, ev.srcElement.textContent);\n"
				+ "}});");
		
		content.getElement().executeJs(
				"this.addEventListener(\"keydown\", (event) => {\n"
				+ "  if (event.key === \"@\") {\n"
				+ "    const range = window.getSelection().getRangeAt(0);\n"
				+ "    const marker = document.createElement(\"span\");\n"
				+ "    marker.id = \"mention-marker\";\n"
				+ "    marker.textContent = \"@\";\n"
				+ "    marker.style.color = \"blue\";\n"
				+ "    range.insertNode(marker);\n"
				+ "    this.parentNode.$server.updateContent(this.innerHTML);\n"
				+ "    this.parentNode.$server.showPopup();\n"
				+ "  }\n"
				+ "});");
		this.items = items;
	}
	
	/**
	 * Process the value finding existing tags and decorates them with the span that will trigger the tag popup
	 * @param value
	 */
	void processAndSetValue(String value) {
		for (T item : items) {
			String itemLabel = labelGenerator.apply(item);
			int index = value.indexOf(itemLabel);
			while (index >= 0) {
				String decoratedLabel = decorateWithSpan(itemLabel);
				value = value.substring(0, index) + decoratedLabel + value.substring(index + itemLabel.length());
				index = value.indexOf(itemLabel, index + decoratedLabel.length());
			}
		}
		super.setValue(value);
		content.getElement().executeJs("this.innerHTML=$0", value);
	}
	
	/**
	 * Updates the value of the component after the span has been created from the client side
	 * @param htmlContent
	 */
	@ClientCallable
	void updateContent(String htmlContent) {
	    super.setValue(htmlContent);
	}
	
	@Override
	public void setValue(String value) {
		super.setValue(value);
		processAndSetValue(value);
	}

	/**
	 * Shows the popup for selecting the tag after the at key was pressed from the client side
	 */
	@ClientCallable
	void showPopup() {
		Popup popup = new Popup();
		popup.setFor("mention-marker");
		AbstractField<?,T> selector = createSelector();
		popup.add(selector);
		selector.addValueChangeListener(ev->{
			TaggableTextArea.this.replaceTag(labelGenerator.apply(ev.getValue()));
			popup.getElement().removeFromParent();
		});
		popup.setOpened(true);
		this.getElement().appendChild(popup.getElement());
		popup.setCloseOnClick(true);
		popup.getElement().executeJs("return;").then((ev)->selector.getElement().executeJs("this.scrollIntoView({ behavior: \"auto\", block: \"start\", inline: \"nearest\" })"));;
	}


	/**
	 * Shows the popup that will be shown after the tag was clicked
	 * 
	 * @param tagId
	 * @param tagName
	 */
	@ClientCallable
	void showTagPopup(String tagId, String tagName) {
		for (T item : items) {
			if (tagName.equals(labelGenerator.apply(item))) {
				if (!tagPopupVisibilityFunction.apply(item)) {
					return;
				}
			}
		}
		this.getElement().executeJs(""
		        + "let result = false;\n"
		        + "for (let child of this.children) {\n"
		        + "    if (child.tagName.toLowerCase() === 'vcf-popup') {\n"
		        + "        if (child._targetElement && child._targetElement.id === $0) {\n"
		        + "            result = true;\n"
		        + "            break;\n"
		        + "        }\n"
		        + "    }\n"
		        + "}\n"
		        + "return result;", tagId).then(result -> {
					if (!result.asBoolean()) {
						Popup popup = new Popup();
						popup.setFor(tagId);
						T relatedItem = null;
						for (T item : items) {
							String label = labelGenerator.apply(item);
							if (label.equals(tagName)) {
								relatedItem = item;
							}
						}
						Component contentComponent = createTagPopupContent(relatedItem);
						popup.add(contentComponent);
						popup.setOpened(true);
						this.getElement().appendChild(popup.getElement());
						popup.setCloseOnClick(true);
					}
				});;
	}
	
	/**
	 * Supplier to decide to show the tag popup depending on the item
	 */
	public void setTagPopupFor(SerializableFunction<T,Boolean> tagPopupVisibilityFunction) {
		this.tagPopupVisibilityFunction  = tagPopupVisibilityFunction;
	}
	
	/**
	 * Shows a component that will be used as the popup content after the tag was clicked. By default
	 * it will show a simple span with the tag converted to string, should be overwritten
	 * @param relatedItem
	 * @return
	 */
	protected Component createTagPopupContent(T relatedItem) {
		return new Span(""+relatedItem);
	}

	public List<T> obtainUsedTags() {
		List<T> result = new ArrayList<T>();
		String content = this.getValue();
		for (T item : items) {
			String itemLabel = labelGenerator.apply(item);
			int index = content.indexOf(itemLabel);
			while (index >= 0) {
				result.add(item);
				index = content.indexOf(itemLabel, index + itemLabel.length());
			}
		}
		return result;
	}
	
	/**
	 * Decorates the label with the span that can be styled to highlight the tag. It can be also overwritten, but
	 * it should always contain a unique id so the popup can be opened in the correct position
	 * @param label
	 * @return
	 */
	protected String decorateWithSpan(String label) {
		return "<span class=\"mention-highlight\" style=\"color: blue;\" id=\"span-" + UUID.randomUUID() + "\">" + label + "</span>";
	}
	
	/**
	 * Creates the selector field that will be shown inside the popup to select the tag. By default
	 * it will return a listbox containing the items converted to string with the toString() method.
	 * It can be overwritten so it uses a different component for selecting the tags.
	 * @return
	 */
	protected AbstractField<?,T> createSelector() {
		ListBox<T> listBox = new ListBox<>();
		listBox.setItems(this.items);
		listBox.getElement().executeJs("this.focus()");
		listBox.setRenderer(new ComponentRenderer<>(item -> {
		    HorizontalLayout row = new HorizontalLayout();
		    row.setAlignItems(FlexComponent.Alignment.CENTER);

		    Span name = new Span(""+item);

		    VerticalLayout column = new VerticalLayout(name);
		    column.setPadding(false);
		    column.setSpacing(false);

		    row.add(column);
		    row.getStyle().set("line-height", "var(--lumo-line-height-m)");
		    return row;
		}));
		return listBox;
	}
	
	/**
	 * Replaces the mention-marker with the span that will contain a unique id, so it can be clicked
	 * @param value
	 */
	private void replaceTag(String value) {
		this.getElement().executeJs(""
				+ "const marker = this.querySelector(\"#mention-marker\");\n"
				+ "if (marker) {\n"
				+ " const span = document.createElement(\"span\");\n"
				+ " span.textContent = $0;\n"
				+ " span.className = \"mention-highlight\";\n"
				+ " span.id = \"span-" + UUID.randomUUID() + "\";\n"
				+ " span.style.color = \"blue\";\n"
				+ " marker.replaceWith(span);\n"
				+ " const cleanTextNode = document.createElement(\"div\");\n"
				+ " cleanTextNode.innerHTML = '&nbsp;';\n"
				+ " cleanTextNode.style.display = \"inline\";"
				+ " span.after(cleanTextNode);"
				+ " const range = document.createRange();\n"
				+ " range.setStartAfter(cleanTextNode);\n"
				+ " range.collapse(true);\n"
				+ " const selection = window.getSelection();\n"
				+ " selection.removeAllRanges();\n"
				+ " selection.addRange(range);\n"
				+ "}", value).then((ev)->content.getElement().executeJs("this.parentNode.$server.updateContent(this.innerHTML);this.focus()"));
	}
	
	/**
	 * Converts the html content to plain text, but adds a br tag before the beginning of each div to preserve line breaks
	 * @returns
	 */
	public String getPlainValue() {
		String htmlValue = getValue();
		htmlValue = htmlValue.replaceAll("<div>", "<div>@@br@@");
		String result = Jsoup.parse(htmlValue).text();
		return result.replaceAll("@@br@@", "<br/>");
	}
	
	@Override
	public void setReadOnly(boolean readonly) {
		super.setReadonly(readonly);
		if (readonly) {
			content.getElement().removeAttribute("contenteditable");
		} else {
			content.getElement().setAttribute("contenteditable", true);
		}
	}
	
	@Override
	public void clear() {
		super.clear();
		content.setText("");
	}

}