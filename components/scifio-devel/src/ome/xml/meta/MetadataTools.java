/*
 * #%L
 * OME SCIFIO package for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2005 - 2012 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package ome.xml.meta;

import loci.formats.IFormatReader;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.meta.MetadataStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.scifio.CoreMetadata;
import ome.scifio.FormatException;
import ome.scifio.common.DateTools;
import ome.scifio.io.Location;
import ome.scifio.services.DependencyException;
import ome.scifio.services.ServiceFactory;
import ome.scifio.util.FormatTools;
import ome.xml.model.*;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.EnumerationException;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.*;
import ome.xml.services.OMEXMLService;

/**
 * A utility class for working with metadata objects,
 * including {@link MetadataStore}, {@link MetadataRetrieve},
 * and OME-XML strings.
 * Most of the methods require the optional {@link loci.formats.ome}
 * package, and optional ome-xml.jar library, to be present at runtime.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/MetadataTools.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/MetadataTools.java;hb=HEAD">Gitweb</a></dd></dl>
 */
public class MetadataTools {
  
  // -- Constants --
  
  private static final Logger LOGGER =
    LoggerFactory.getLogger(MetadataTools.class);
  
  // -- Static fields --

  private static boolean defaultDateEnabled = true;
  
  // -- Utility methods - OME-XML --

  /**
   * Populates the 'pixels' element of the given metadata store, using core
   * metadata from the given reader.
   */
  public static void populatePixels(MetadataStore store, IFormatReader r) {
    populatePixels(store, r, false, true);
  }

  /**
   * Populates the 'pixels' element of the given metadata store, using core
   * metadata from the given reader.  If the 'doPlane' flag is set,
   * then the 'plane' elements will be populated as well.
   */
  public static void populatePixels(MetadataStore store, IFormatReader r,
    boolean doPlane)
  {
    populatePixels(store, r, doPlane, true);
  }

  /**
   * Populates the 'pixels' element of the given metadata store, using core
   * metadata from the given reader.  If the 'doPlane' flag is set,
   * then the 'plane' elements will be populated as well.
   * If the 'doImageName' flag is set, then the image name will be populated
   * as well.  By default, 'doImageName' is true.
   */
  public static void populatePixels(MetadataStore store, IFormatReader r,
    boolean doPlane, boolean doImageName)
  {
    if (store == null || r == null) return;
    int oldSeries = r.getSeries();
    for (int i=0; i<r.getSeriesCount(); i++) {
      r.setSeries(i);

      String imageName = null;
      if (doImageName) {
        Location f = new Location(r.getCurrentFile());
        imageName = f.getName();
      }
      String pixelType = FormatTools.getPixelTypeString(r.getPixelType());

      populateMetadata(store, r.getCurrentFile(), i, imageName,
        r.isLittleEndian(), r.getDimensionOrder(), pixelType, r.getSizeX(),
        r.getSizeY(), r.getSizeZ(), r.getSizeC(), r.getSizeT(),
        r.getRGBChannelCount());

      try {
        OMEXMLService service =
          new ServiceFactory().getInstance(OMEXMLService.class);
        if (service.isOMEXMLRoot(store.getRoot())) {
          MetadataStore baseStore = r.getMetadataStore();
          if (service.isOMEXMLMetadata(baseStore)) {
            ((OMEXMLMetadataImpl) baseStore).resolveReferences();
          }

          OME root = (OME) store.getRoot();
          BinData bin = root.getImage(i).getPixels().getBinData(0);
          bin.setLength(new NonNegativeLong(0L));
          store.setRoot(root);
        }
      }
      catch (DependencyException exc) {
        LOGGER.warn("Failed to set BinData.Length", exc);
      }

      if (doPlane) {
        for (int q=0; q<r.getImageCount(); q++) {
          int[] coords = r.getZCTCoords(q);
          store.setPlaneTheZ(new NonNegativeInteger(coords[0]), i, q);
          store.setPlaneTheC(new NonNegativeInteger(coords[1]), i, q);
          store.setPlaneTheT(new NonNegativeInteger(coords[2]), i, q);
        }
      }
    }
    r.setSeries(oldSeries);
  }

  /**
   * Populates the given {@link MetadataStore}, for the specified series, using
   * the provided values.
   * <p>
   * After calling this method, the metadata store will be sufficiently
   * populated for use with an {@link IFormatWriter} (assuming it is also a
   * {@link MetadataRetrieve}).
   * </p>
   */
  public static void populateMetadata(MetadataStore store, int series,
    String imageName, boolean littleEndian, String dimensionOrder,
    String pixelType, int sizeX, int sizeY, int sizeZ, int sizeC, int sizeT,
    int samplesPerPixel)
  {
    populateMetadata(store, null, series, imageName, littleEndian,
      dimensionOrder, pixelType, sizeX, sizeY, sizeZ, sizeC, sizeT,
      samplesPerPixel);
  }
  
  /**
   * Populates the given {@link MetadataStore}, for the specified series, using
   * the values from the provided {@link CoreMetadata}.
   * <p>
   * After calling this method, the metadata store will be sufficiently
   * populated for use with an {@link IFormatWriter} (assuming it is also a
   * {@link MetadataRetrieve}).
   * </p>
   */
  public static void populateMetadata(MetadataStore store, int series,
    String imageName, loci.formats.CoreMetadata coreMeta)
  {
    final String pixelType = FormatTools.getPixelTypeString(coreMeta.pixelType);
    final int effSizeC = coreMeta.imageCount / coreMeta.sizeZ / coreMeta.sizeT;
    final int samplesPerPixel = coreMeta.sizeC / effSizeC;
    populateMetadata(store, null, series, imageName, coreMeta.littleEndian,
      coreMeta.dimensionOrder, pixelType, coreMeta.sizeX, coreMeta.sizeY,
      coreMeta.sizeZ, coreMeta.sizeC, coreMeta.sizeT, samplesPerPixel);
  }
  
  /**
   * Populates the given {@link MetadataStore}, for the specified series, using
   * the provided values.
   * <p>
   * After calling this method, the metadata store will be sufficiently
   * populated for use with an {@link IFormatWriter} (assuming it is also a
   * {@link MetadataRetrieve}).
   * </p>
   */
  public static void populateMetadata(MetadataStore store, String file,
    int series, String imageName, boolean littleEndian, String dimensionOrder,
    String pixelType, int sizeX, int sizeY, int sizeZ, int sizeC, int sizeT,
    int samplesPerPixel)
  {
    store.setImageID(createLSID("Image", series), series);
    setDefaultCreationDate(store, file, series);
    if (imageName != null) store.setImageName(imageName, series);
    populatePixelsOnly(store, series, littleEndian, dimensionOrder, pixelType,
      sizeX, sizeY, sizeZ, sizeC, sizeT, samplesPerPixel);
  }

  public static void populatePixelsOnly(MetadataStore store, IFormatReader r) {
    int oldSeries = r.getSeries();
    for (int i=0; i<r.getSeriesCount(); i++) {
      r.setSeries(i);

      String pixelType = FormatTools.getPixelTypeString(r.getPixelType());

      populatePixelsOnly(store, i, r.isLittleEndian(), r.getDimensionOrder(),
        pixelType, r.getSizeX(), r.getSizeY(), r.getSizeZ(), r.getSizeC(),
        r.getSizeT(), r.getRGBChannelCount());
    }
    r.setSeries(oldSeries);
  }

  public static void populatePixelsOnly(MetadataStore store, int series,
    boolean littleEndian, String dimensionOrder, String pixelType, int sizeX,
    int sizeY, int sizeZ, int sizeC, int sizeT, int samplesPerPixel)
  {
    store.setPixelsID(createLSID("Pixels", series), series);
    store.setPixelsBinDataBigEndian(!littleEndian, series, 0);
    try {
      store.setPixelsDimensionOrder(
        DimensionOrder.fromString(dimensionOrder), series);
    }
    catch (EnumerationException e) {
      LOGGER.warn("Invalid dimension order: " + dimensionOrder, e);
    }
    try {
      store.setPixelsType(PixelType.fromString(pixelType), series);
    }
    catch (EnumerationException e) {
      LOGGER.warn("Invalid pixel type: " + pixelType, e);
    }
    store.setPixelsSizeX(new PositiveInteger(sizeX), series);
    store.setPixelsSizeY(new PositiveInteger(sizeY), series);
    store.setPixelsSizeZ(new PositiveInteger(sizeZ), series);
    store.setPixelsSizeC(new PositiveInteger(sizeC), series);
    store.setPixelsSizeT(new PositiveInteger(sizeT), series);
    int effSizeC = sizeC / samplesPerPixel;
    for (int i=0; i<effSizeC; i++) {
      store.setChannelID(createLSID("Channel", series, i),
        series, i);
      store.setChannelSamplesPerPixel(new PositiveInteger(samplesPerPixel),
        series, i);
    }
  }

  /**
   * Disables the setting of a default creation date.
   *
   * By default, missing creation dates will be replaced with the corresponding
   * file's last modification date, or the current time if the modification
   * date is not available.
   *
   * Calling this method with the 'enabled' parameter set to 'false' causes
   * missing creation dates to be left as null.
   *
   * @param enabled See above.
   * @see #setDefaultCreationDate(MetadataStore, String, int)
   */
  public static void setDefaultDateEnabled(boolean enabled) {
    defaultDateEnabled = enabled;
  }

  /**
   * Sets a default creation date.  If the named file exists, then the creation
   * date is set to the file's last modification date.  Otherwise, it is set
   * to the current date.
   *
   * @see #setDefaultDateEnabled(boolean)
   */
  public static void setDefaultCreationDate(MetadataStore store, String id,
    int series)
  {
    if (!defaultDateEnabled) {
      return;
    }
    Location file = id == null ? null : new Location(id).getAbsoluteFile();
    long time = System.currentTimeMillis();
    if (file != null && file.exists()) time = file.lastModified();
    store.setImageAcquisitionDate(new Timestamp(DateTools.convertDate(
        time, DateTools.UNIX)), series);
  }
  
  /**
  *
  * @throws FormatException if there is a missing metadata field,
  *   or the metadata object is uninitialized
  */
 public static void verifyMinimumPopulated(MetadataRetrieve src)
   throws FormatException
 {
   verifyMinimumPopulated(src, 0);
 }

 /**
  *
  * @throws FormatException if there is a missing metadata field,
  *   or the metadata object is uninitialized
  */
 public static void verifyMinimumPopulated(MetadataRetrieve src, int n)
   throws FormatException
 {
   if (src == null) {
     throw new FormatException("Metadata object is null; " +
         "call IFormatWriter.setMetadataRetrieve() first");
   }
   if (src instanceof MetadataStore
       && ((MetadataStore) src).getRoot() == null) {
     throw new FormatException("Metadata object has null root; " +
       "call IMetadata.createRoot() first");
   }
   if (src.getImageID(n) == null) {
     throw new FormatException("Image ID #" + n + " is null");
   }
   if (src.getPixelsID(n) == null) {
     throw new FormatException("Pixels ID #" + n + " is null");
   }
   for (int i=0; i<src.getChannelCount(n); i++) {
     if (src.getChannelID(n, i) == null) {
       throw new FormatException("Channel ID #" + i + " in Image #" + n +
         " is null");
     }
   }
   if (src.getPixelsBinDataBigEndian(n, 0) == null) {
     throw new FormatException("BigEndian #" + n + " is null");
   }
   if (src.getPixelsDimensionOrder(n) == null) {
     throw new FormatException("DimensionOrder #" + n + " is null");
   }
   if (src.getPixelsType(n) == null) {
     throw new FormatException("PixelType #" + n + " is null");
   }
   if (src.getPixelsSizeC(n) == null) {
     throw new FormatException("SizeC #" + n + " is null");
   }
   if (src.getPixelsSizeT(n) == null) {
     throw new FormatException("SizeT #" + n + " is null");
   }
   if (src.getPixelsSizeX(n) == null) {
     throw new FormatException("SizeX #" + n + " is null");
   }
   if (src.getPixelsSizeY(n) == null) {
     throw new FormatException("SizeY #" + n + " is null");
   }
   if (src.getPixelsSizeZ(n) == null) {
     throw new FormatException("SizeZ #" + n + " is null");
   }
 }
 

 /**
  * Adjusts the given dimension order as needed so that it contains exactly
  * one of each of the following characters: 'X', 'Y', 'Z', 'C', 'T'.
  */
 public static String makeSaneDimensionOrder(String dimensionOrder) {
   String order = dimensionOrder.toUpperCase();
   order = order.replaceAll("[^XYZCT]", "");
   String[] axes = new String[] {"X", "Y", "C", "Z", "T"};
   for (String axis : axes) {
     if (order.indexOf(axis) == -1) order += axis;
     while (order.indexOf(axis) != order.lastIndexOf(axis)) {
       order = order.replaceFirst(axis, "");
     }
   }
   return order;
 }
 
 /**
  * Constructs an LSID, given the object type and indices.
  * For example, if the arguments are "Detector", 1, and 0, the LSID will
  * be "Detector:1:0".
  */
 public static String createLSID(String type, int... indices) {
   StringBuffer lsid = new StringBuffer(type);
   for (int index : indices) {
     lsid.append(":");
     lsid.append(index);
   }
   return lsid.toString();
 }
  
}