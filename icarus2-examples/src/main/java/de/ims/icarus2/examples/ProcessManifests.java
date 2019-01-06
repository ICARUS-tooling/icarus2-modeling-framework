/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 */
package de.ims.icarus2.examples;

import static java.util.Objects.requireNonNull;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;

import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ValueManifest;
import de.ims.icarus2.model.manifest.api.ValueRange;
import de.ims.icarus2.model.manifest.api.ValueSet;

/**
 * @author Markus Gärtner
 *
 */
public class ProcessManifests {

	public static void main(String[] args) {

	}

	class AnnotationValuePicker {

		private final AnnotationManifest manifest;
		private final JComponent container;

		public AnnotationValuePicker(JComponent container, AnnotationManifest manifest) {
			this.container = requireNonNull(container);
			this.manifest = requireNonNull(manifest);

			// Build components
			manifest.getValueSet().ifPresent(this::addChoice);
		}

		void addUIElement(JComponent component) {
			// Add the given component to this UI
			container.add(component);
		}

		void addChoice(ValueSet valueSet) {
			JComboBox<Object> comboBox = new JComboBox<>(valueSet.getValues());
			comboBox.setEditable(manifest.isAllowUnknownValues());
			manifest.getNoEntryValue().ifPresent(comboBox::setSelectedItem);
			comboBox.setRenderer(new ValueRenderer());

			addUIElement(comboBox);
		}

		void addRange(ValueRange valueRange) {

		}

		void addFreeText() {

		}
	}

	class ValueRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if(value instanceof ValueManifest) {
				ValueManifest vm = (ValueManifest) value;
				vm.getName().ifPresent(this::setText);
				vm.getDescription().ifPresent(this::setToolTipText);
				vm.getIcon().ifPresent(this::setIcon);
			}

			return this;
		}
	}
}
