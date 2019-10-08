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

import static de.ims.icarus2.util.Conditions.checkArgument;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ManifestFactory;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ValueManifest;
import de.ims.icarus2.model.manifest.api.ValueRange;
import de.ims.icarus2.model.manifest.api.ValueSet;
import de.ims.icarus2.model.manifest.standard.DefaultManifestFactory;
import de.ims.icarus2.model.manifest.standard.DefaultManifestRegistry;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.ManifestBuilder;
import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 *
 */
public class ICARUS2Sample_04_ProcessManifestsForGui {

	public static void main(String[] args) {

		// Set up the factory
		ManifestRegistry registry = new DefaultManifestRegistry();
		ManifestLocation location = ManifestLocation.builder().virtual().build();
		ManifestFactory factory = new DefaultManifestFactory(location, registry);

		List<AnnotationManifest> annotationManifests = new ArrayList<>();

		// Create example manifest
		try(ManifestBuilder builder = new ManifestBuilder(factory)) {

			/*
			 * Example of a classic category annotation with a fixed set of
			 * available values to choose from. In addition we allow "unseen"
			 * (i.e. new) values.
			 */
			annotationManifests.add(builder.create(AnnotationManifest.class, "anno1",
						Options.of(ManifestFactory.OPTION_VALUE_TYPE, ValueType.STRING))
					.setKey("stringValues")
					.setName("Fixed Annotation")
					.setValueType(ValueType.STRING)
					.setValueSet(builder.create(ValueSet.class)
							.addValue(builder.create(ValueManifest.class)
									.setValue("xyz")
									.setName("value1")
									.setDescription("a specific value"))
							.addValue(builder.create(ValueManifest.class)
									.setValue("foo")
									.setName("value2")
									.setDescription("another cool value"))
							.addValue(builder.create(ValueManifest.class)
									.setValue("bar")
									.setName("value3")
									.setDescription("the most awesome value of them all ^^")))
					.setAllowUnknownValues(true));

			/*
			 * Numeric annotation that follows a bounded-range model to
			 * limit the value space.
			 */
			annotationManifests.add(builder.create(AnnotationManifest.class, "anno2",
						Options.of(ManifestFactory.OPTION_VALUE_TYPE, ValueType.INTEGER))
					.setKey("intValues")
					.setName("Range Annotation")
					.setValueRange(builder.create(ValueRange.class,
								Options.of(ManifestFactory.OPTION_VALUE_TYPE, ValueType.INTEGER))
							.setLowerBound(Integer.valueOf(10))
							.setUpperBound(Integer.valueOf(100))
							.setStepSize(Integer.valueOf(5)))
					.setNoEntryValue(Integer.valueOf(45))
					.setAllowUnknownValues(true));

			/*
			 * A completely unbounded textual annotation, allowing arbitrary
			 * text.
			 */
			annotationManifests.add(builder.create(AnnotationManifest.class, "anno3",
						Options.of(ManifestFactory.OPTION_VALUE_TYPE, ValueType.INTEGER))
					.setKey("freeValues")
					.setName("Free Annotation")
					.setNoEntryValue("Nothing set yet...")
					.setAllowUnknownValues(true));
		}

		// Show a simple GUI
		SwingUtilities.invokeLater(() -> initAndShowGui(annotationManifests));
	}

	private static void initAndShowGui(List<AnnotationManifest> annotationManifests) {

		JFrame frame = new JFrame("ICARUS2 Example - "+ICARUS2Sample_04_ProcessManifestsForGui.class.getSimpleName());

		frame.add(new AnnotationValuePicker(annotationManifests));

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	static class AnnotationValuePicker extends JPanel implements ActionListener,
			ChangeListener, DocumentListener {
		private static final long serialVersionUID = -8569680717430416493L;

		private final JTextArea textArea;
		private final JPanel contentPanel;

		private final Object MANIFEST_KEY = "manifest";

		public AnnotationValuePicker(List<AnnotationManifest> annotationManifests) {
			super(new BorderLayout());

			textArea = new JTextArea(10, 50);

			contentPanel = new JPanel();
			contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));

			for(AnnotationManifest annotationManifest : annotationManifests) {
				JPanel panel = new JPanel();
				panel.setBorder(BorderFactory.createTitledBorder(
						annotationManifest.getName().orElse("unnamed")));
				annotationManifest.getDescription().ifPresent(panel::setToolTipText);
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

				annotationManifest.getValueSet().ifPresent(
						set -> addChoice(annotationManifest, set, panel));
				annotationManifest.getValueRange().ifPresent(
						range -> addRange(annotationManifest, range, panel));

				if(panel.getComponentCount()==0 && annotationManifest.isAllowUnknownValues()) {
					addFreeText(annotationManifest, panel);
				}

				contentPanel.add(panel);
			}

			add(contentPanel, BorderLayout.CENTER);
			add(textArea, BorderLayout.SOUTH);
		}

		private void addChoice(AnnotationManifest manifest, ValueSet valueSet, JComponent container) {
			JComboBox<Object> comboBox = new JComboBox<>(valueSet.getValues());
			comboBox.setEditable(manifest.isAllowUnknownValues());
			manifest.getNoEntryValue().ifPresent(comboBox::setSelectedItem);
			comboBox.setRenderer(new ValueRenderer());
			comboBox.addActionListener(this);
			comboBox.putClientProperty(MANIFEST_KEY, manifest);

			container.add(comboBox);
		}

		private void addRange(AnnotationManifest manifest, ValueRange valueRange, JComponent container) {
			checkArgument("Value type must be a number",
					Number.class.isAssignableFrom(valueRange.getValueType().getBaseClass()));

			SpinnerNumberModel model = new SpinnerNumberModel();
			valueRange.<Comparable<?>>getLowerBound().ifPresent(model::setMinimum);
			valueRange.<Comparable<?>>getUpperBound().ifPresent(model::setMaximum);
			valueRange.<Number>getStepSize().ifPresent(model::setStepSize);
			manifest.getNoEntryValue().ifPresent(model::setValue);

			JSpinner spinner = new JSpinner(model);
			spinner.addChangeListener(this);
			spinner.putClientProperty(MANIFEST_KEY, manifest);

			container.add(spinner);
		}

		private void addFreeText(AnnotationManifest manifest, JComponent container) {
			JTextField textField = new JTextField(20);
			manifest.getNoEntryValue().ifPresent(noEntryValue -> textField.setText(noEntryValue.toString()));
			textField.getDocument().addDocumentListener(this);
			textField.putClientProperty(MANIFEST_KEY, manifest);

			container.add(new JScrollPane(textField));
		}

		private void displayValue(JComponent source, Object value) {
			AnnotationManifest manifest = (AnnotationManifest) source.getClientProperty(MANIFEST_KEY);
			String text = String.format("%s -> %s%n",
					manifest.getName(), String.valueOf(value));

			textArea.append(text);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JComboBox<?> source = (JComboBox<?>) e.getSource();
			displayValue(source, source.getSelectedItem());
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			JSpinner source = (JSpinner) e.getSource();
			displayValue(source, source.getValue());
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			textArea.append("Free text changed\n");
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			changedUpdate(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			changedUpdate(e);

		}
	}

	static class ValueRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1302077701744189734L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if(value instanceof ValueManifest) {
				ValueManifest vm = (ValueManifest) value;
				vm.getName().ifPresent(this::setText);
				vm.getDescription().ifPresent(this::setToolTipText);
			}

			return this;
		}
	}
}
