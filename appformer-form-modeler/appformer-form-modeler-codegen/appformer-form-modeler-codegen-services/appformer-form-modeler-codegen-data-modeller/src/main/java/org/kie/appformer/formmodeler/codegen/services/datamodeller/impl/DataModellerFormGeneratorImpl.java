/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.appformer.formmodeler.codegen.services.datamodeller.impl;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.apache.commons.lang3.text.WordUtils;
import org.kie.appformer.formmodeler.codegen.FormSourcesGenerator;
import org.kie.appformer.formmodeler.codegen.services.datamodeller.DataModellerFormGenerator;
import org.kie.workbench.common.forms.data.modeller.model.DataObjectFormModel;
import org.kie.workbench.common.forms.data.modeller.service.impl.DataModellerFieldGenerator;
import org.kie.workbench.common.forms.editor.service.VFSFormFinderService;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.EmbedsForm;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.EntityRelationField;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.TableColumnMeta;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.multipleSubform.definition.MultipleSubFormFieldDefinition;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.subForm.definition.SubFormFieldDefinition;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.kie.workbench.common.forms.model.FormDefinition;
import org.kie.workbench.common.forms.service.FieldManager;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.vfs.Path;

public class DataModellerFormGeneratorImpl implements DataModellerFormGenerator {
    private static transient Logger log = LoggerFactory.getLogger( DataModellerFormGeneratorImpl.class );

    @Inject
    protected DataModelerService dataModelerService;

    @Inject
    protected KieProjectService projectService;

    @Inject
    protected FieldManager fieldManager;

    @Inject
    protected FormSourcesGenerator formSourcesGenerator;

    @Inject
    protected DataModellerFieldGenerator fieldGenerator;

    @Inject
    protected VFSFormFinderService vfsFormFinderService;

    @Override
    public void generateFormForDataObject( DataObject dataObject, Path path ) {

        if (dataObject.getProperties().isEmpty()) return;

        String modelName = WordUtils.uncapitalize( dataObject.getName() );

        DataObjectFormModel model = new DataObjectFormModel( modelName, dataObject.getClassName() );

        FormDefinition form = new FormDefinition( model );

        form.setId( dataObject.getClassName() );

        form.setName( dataObject.getName() );

        List<FieldDefinition> availabeFields = fieldGenerator.getFieldsFromDataObject(modelName, dataObject);

        for (FieldDefinition field : availabeFields ) {
            if (field instanceof EmbedsForm) {
                if ( !loadEmbeddedFormConfig( field, path ) ) continue;
            }
            form.getFields().add( field );
        }

        if (form.getFields().isEmpty()) return;

        formSourcesGenerator.generateEntityFormSources(form, path);

    }

    protected boolean loadEmbeddedFormConfig ( FieldDefinition field, Path path ) {
        if ( !(field instanceof EmbedsForm) ) return false;

        List<FormDefinition> subForms = vfsFormFinderService.findFormsForType( field.getStandaloneClassName(), path );

        if ( subForms == null || subForms.isEmpty() ) {
            return false;
        }

        if ( field instanceof  MultipleSubFormFieldDefinition ) {
            MultipleSubFormFieldDefinition multipleSubFormFieldDefinition = (MultipleSubFormFieldDefinition) field;
            FormDefinition form = subForms.get( 0 );
            multipleSubFormFieldDefinition.setCreationForm( form.getId() );
            multipleSubFormFieldDefinition.setEditionForm( form.getId() );

            List<TableColumnMeta> columnMetas = new ArrayList<>();
            for ( FieldDefinition nestedField : form.getFields() ) {
                if ( nestedField instanceof EntityRelationField ) {
                    continue;
                }
                TableColumnMeta meta = new TableColumnMeta( nestedField.getLabel(), nestedField.getBinding() );
                columnMetas.add( meta );
            }

            multipleSubFormFieldDefinition.setColumnMetas( columnMetas );
        } else {
            SubFormFieldDefinition subFormField = (SubFormFieldDefinition) field;
            subFormField.setNestedForm( subForms.get( 0 ).getId() );
        }

        return true;
    }
}
