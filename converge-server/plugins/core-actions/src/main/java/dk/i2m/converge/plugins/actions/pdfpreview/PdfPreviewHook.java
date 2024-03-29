/*
 * Copyright (C) 2012 Interactive Media Management
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.i2m.converge.plugins.actions.pdfpreview;

import dk.i2m.converge.core.annotations.CatalogueAction;
import dk.i2m.converge.core.content.catalogue.CatalogueHookInstance;
import dk.i2m.converge.core.content.catalogue.MediaItemRendition;
import dk.i2m.converge.core.content.catalogue.Rendition;
import dk.i2m.converge.core.logging.LogSeverity;
import dk.i2m.converge.core.plugin.CatalogueEvent;
import dk.i2m.converge.core.plugin.CatalogueEventException;
import dk.i2m.converge.core.plugin.CatalogueHook;
import dk.i2m.converge.core.plugin.PluginContext;
import dk.i2m.converge.core.utils.ImageUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * {@link CatalogueHook} for generating a preview of a PDF file. Uses
 * {@link PDF Renderer https://pdf-renderer.dev.java.net/} supporting the PDF
 * 1.4 standard.
 *
 * @author Allan Lykke Christensen
 */
@CatalogueAction
public class PdfPreviewHook extends CatalogueHook {

    private ResourceBundle bundle = ResourceBundle.getBundle(
            "dk.i2m.converge.plugins.actions.pdfpreview.Messages");

    private Map<String, String> instanceProperties =
            new HashMap<String, String>();

    private Map<String, String> availableProperties = null;

    private void validateProperties() {
    }

    enum Property {

        ORIGINAL_RENDITION,
        GENERATE_RENDITION,
        GENERATE_RENDITION_USE_DEFAULT_DIMENSION,
        GENERATE_RENDITION_USE_ORIGINAL_DIMENSION,
        GENERATE_RENDITION_WIDTH,
        GENERATE_RENDITION_HEIGHT,
        ENABLE_ON_UPDATE
    }

    private boolean isPropertySet(Property p) {
        return instanceProperties.containsKey(p.name());
    }

    private String getProperty(Property p) {
        return instanceProperties.get(p.name());
    }

    private Boolean getPropertyAsBoolean(Property p) {
        return Boolean.parseBoolean(getProperty(p));
    }

    private Long getPropertyAsLong(Property p) {
        return Long.valueOf(getProperty(p));
    }

    private Integer getPropertyAsInteger(Property p) {
        return Integer.valueOf(getProperty(p));
    }

    @Override
    public void execute(PluginContext ctx, CatalogueEvent event,
            CatalogueHookInstance instance) throws CatalogueEventException {
        instanceProperties = instance.getPropertiesAsMap();

        validateProperties();

        boolean executeOnUpdate = false;

        if (isPropertySet(Property.ENABLE_ON_UPDATE)) {
            executeOnUpdate = getPropertyAsBoolean(Property.ENABLE_ON_UPDATE);
        }

        // Check that we only re-act to the Upload of new or update renditions
        if (event.getType() != CatalogueEvent.Event.UploadRendition && event.
                getType() != CatalogueEvent.Event.UpdateRendition) {
            return;
        }

        // Check that we only re-act to the Upload of updated renditions if the ENABLE_ON_UPDATE was set
        if (!executeOnUpdate && event.getType()
                == CatalogueEvent.Event.UpdateRendition) {
            return;
        }

        MediaItemRendition uploadRendition = event.getRendition();
        Rendition rendition = uploadRendition.getRendition();

        if (!rendition.getName().equalsIgnoreCase(getProperty(
                Property.ORIGINAL_RENDITION))) {
            return;
        }

        // Which rendition should be generated
        String generateRenditionName = getProperty(Property.GENERATE_RENDITION);
        Rendition generateRendition = ctx.findRenditionByName(
                generateRenditionName);

        if (generateRendition == null) {
            throw new CatalogueEventException("Rendition "
                    + generateRenditionName + " does not exist");
        }

        // Determine dimensions of preview
        boolean useGenerateDimensions = getPropertyAsBoolean(
                Property.GENERATE_RENDITION_USE_DEFAULT_DIMENSION);
        boolean useDefaultimensions = getPropertyAsBoolean(
                Property.GENERATE_RENDITION_USE_ORIGINAL_DIMENSION);

        int width = 0;
        int height = 0;

        if (useGenerateDimensions) {
            width = generateRendition.getDefaultWidth();
            height = generateRendition.getDefaultHeight();
        } else if (useDefaultimensions) {
            width = 0;
            height = 0;
        } else {
            if (isPropertySet(Property.GENERATE_RENDITION_WIDTH)) {
                width = getPropertyAsInteger(Property.GENERATE_RENDITION_WIDTH);
            }
            if (isPropertySet(Property.GENERATE_RENDITION_HEIGHT)) {
                height =
                        getPropertyAsInteger(Property.GENERATE_RENDITION_HEIGHT);
            }
        }

        PdfPreviewExtractor extractor = new PdfPreviewExtractor(uploadRendition.
                getFileLocation());

        try {
            byte[] preview = extractor.extract();
            ctx.log(LogSeverity.FINE, "Finished extracting preview for "
                    + uploadRendition.getFileLocation(), instance, instance.
                    getId());

            byte[] filedata = ImageUtils.generateThumbnail(preview, width,
                    height, 100);

            // Create temporary file to store the still
            String prefix = "xxx-" + uploadRendition.getMediaItem().getId();
            String suffix = "" + generateRendition.getId();
            try {
                File tempFile = File.createTempFile(prefix, suffix);
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(filedata);
                fos.close();

                // Create the Media Item Rendition
                MediaItemRendition mir = ctx.createMediaItemRendition(tempFile,
                        event.getItem().getId(), generateRendition.getId(),
                        tempFile.getName() + ".png", "image/png");
                ctx.log(LogSeverity.FINE, "Preview successfully generated for "
                        + uploadRendition.getAbsoluteFilename(), instance,
                        instance.getId());
            } catch (IOException ex) {
                throw new CatalogueEventException(ex);
            } catch (IllegalArgumentException ex) {
                throw new CatalogueEventException("Could not resize image", ex);
            }

        } catch (IOException ex) {
            ctx.log(LogSeverity.WARNING, "Could not extract preview for "
                    + uploadRendition.getAbsoluteFilename() + ". " + ex.
                    getMessage(), instance, instance.getId());
        } catch (InterruptedException ex) {
            ctx.log(LogSeverity.WARNING, "Could not resize extracted preview for "
                    + uploadRendition.getAbsoluteFilename() + ". " + ex.
                    getMessage(), instance, instance.getId());
        }
    }

    @Override
    public Map<String, String> getAvailableProperties() {
        if (availableProperties == null) {
            availableProperties = new LinkedHashMap<String, String>();
            for (Property p : Property.values()) {
                availableProperties.put(bundle.getString(p.name()), p.name());
            }
        }
        return availableProperties;
    }

    @Override
    public String getName() {
        return bundle.getString("PLUGIN_NAME");
    }

    @Override
    public String getAbout() {
        return bundle.getString("PLUGIN_ABOUT");
    }

    @Override
    public String getDescription() {
        return bundle.getString("PLUGIN_DESCRIPTION");
    }

    @Override
    public String getVendor() {
        return bundle.getString("PLUGIN_VENDOR");
    }

    @Override
    public Date getDate() {
        try {
            SimpleDateFormat format =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return format.parse(bundle.getString("PLUGIN_BUILD_TIME"));
        } catch (Exception ex) {
            return Calendar.getInstance().getTime();
        }
    }

    @Override
    public ResourceBundle getBundle() {
        return bundle;
    }

    @Override
    public boolean isSupportBatch() {
        return true;
    }
}
