/*
 * #%L
 * OME SCIFIO package for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2005 - 2013 Open Microscopy Environment:
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

package ome.scifio.filters;

import java.util.List;

import ome.scifio.Metadata;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

import net.imglib2.meta.AxisType;

/**
 * {@link ome.scifio.filters.MetadataWrapper} implementation specifically
 * for use with the {@link ome.scifio.filters.DimensionSwapper}.
 * 
 * @see ome.scifio.filters.MetadataWrapper
 * @see ome.scifio.filters.DimensionSwapper
 * 
 * @author Mark Hiner
 */
@Plugin(type=MetadataWrapper.class, attrs={
  @Attr(name=DimensionSwapperMetadata.METADATA_KEY, value=DimensionSwapperMetadata.METADATA_VALUE)
  })
public class DimensionSwapperMetadata extends AbstractMetadataWrapper {
  
  // -- Constants --
  
  public static final String METADATA_VALUE = "ome.scifio.filters.DimensionSwapper";
  
  // -- Fields --
  
  private List<AxisType>[] outputOrder;

  // -- Constructors --
  
  public DimensionSwapperMetadata() {
    this(null);
  }
  
  public DimensionSwapperMetadata(Metadata metadata) {
    super(metadata);
  }
  
  // -- DimensionSwapperMetadata API --
  
  /**
   * Gets the output order for this dataset. This is an array
   * of axes configurations that will be returned when requested
   * from this Metadata. It may be different than the parsed ("input")
   * axes order for a given image.
   * 
   * @return The output order array for this dataset
   */
  public List<AxisType>[] getOutputOrder() {
    return outputOrder;
  }

  /**
   * Sets the output order for this dataset. Each position of the provided
   * array corresponds to the output order axes for that image index.
   * 
   * @param outputOrder - Array of output orders for this dataset
   */
  public void setOutputOrder(List<AxisType>[] outputOrder) {
    this.outputOrder = outputOrder;
  }
  
  // -- Metadata API Methods --
  
  /*
   * @see ome.scifio.filters.AbstractMetadataWrapper#setChannelDimLengths(int, int[])
   */
  public void setChannelDimLengths(final int imageIndex, final int[] cLengths) {
    super.setChannelDimLengths(imageIndex, cLengths, false);
  }
  
  /*
   * @see ome.scifio.filters.AbstractMetadataWrapper#setChannelDimTypes(int, java.lang.String[])
   */
  public void setChannelDimTypes(final int imageIndex, final String[] cTypes) {
    super.setChannelDimTypes(imageIndex, cTypes, false);
  }
  
  /*
   * @see ome.scifio.filters.AbstractMetadataWrapper#setAxisLengths(int, int[])
   */
  public void setAxisLengths(final int imageIndex, final int[] axisLengths) {
    super.setAxisLengths(imageIndex, axisLengths, false);
  }
  
  /*
   * @see ome.scifio.filters.AbstractMetadataWrapper#setAxisTypes(int, net.imglib2.meta.AxisType[])
   */
  public void setAxisTypes(final int imageIndex, final AxisType[] axisTypes) {
    super.setAxisTypes(imageIndex, axisTypes, false);
  }
}
