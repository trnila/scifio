/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
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

package io.scif.filters;

import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.io.RandomAccessInputStream;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.util.ClassUtils;

/**
 * Abstract superclass for all {@link io.scif.filters.Filter} that delegate to
 * {@link io.scif.Reader} instances.
 * <p>
 * NB: All concrete implementations of this interface should be annotated as
 * {@link Plugin} for automatic discovery.
 * </p>
 * <p>
 * NB: This class attempts to locate a type-matching MetadataWrapper to
 * protectively wrap the wrapped {@code Reader}'s Metadata. If none is found, a
 * reference to the {@code Reader's} Metadata itself is used.
 * </p>
 * 
 * @author Mark Hiner
 * @see io.scif.Reader
 * @see io.scif.filters.Filter
 * @see io.scif.filters.AbstractMetadataWrapper
 */
public abstract class AbstractReaderFilter extends AbstractFilter<Reader>
	implements Reader
{

	// -- Fields --

	/* Need to wrap each Reader's Metadata separately */
	private Metadata wrappedMeta = null;

	private final Class<? extends Metadata> metaClass;

	@Parameter
	private PluginService pluginService;

	// -- Constructor --

	public AbstractReaderFilter() {
		this(null);
	}

	public AbstractReaderFilter(final Class<? extends Metadata> metaClass) {
		super(Reader.class);
		this.metaClass = metaClass;
	}

	// -- AbstractReaderFilter API Methods --

	/**
	 * Allows code to be executed regardless of which {@link #setSource()}
	 * signature is called.
	 * 
	 * @param source - Lowest common denominator of arguments in the
	 *          {@code setSource} series.
	 */
	protected void setSourceHelper(final String source) {

	}

	/**
	 * Allows code to be executed regardless of which {@link #openPlane()}
	 * signature is called.
	 */
	protected void openPlaneHelper() {

	}

	/**
	 * Allows code to be executed regardless of which {@link #readPlane()}
	 * signature is called.
	 */
	protected void readPlaneHelper() {}

	/**
	 * Convenience accessor for the parent's Metadata
	 */
	protected Metadata getParentMeta() {
		return getParent().getMetadata();
	}

	// -- Filter API Methods --

	@Override
	public void setParent(final Object parent) {
		super.setParent(parent);

		final Reader r = (Reader) parent;

		// TODO Maybe cache this result so we don't have to discover every time
		// setparent is called
		// because it will be called frequently, given how MasterFilterHelper is
		// implemented

		final List<PluginInfo<MetadataWrapper>> wrapperInfos =
			getContext().getPluginIndex().getPlugins(MetadataWrapper.class);

		// look for a compatible MetadataWrapper class
		for (final PluginInfo<MetadataWrapper> info : wrapperInfos) {
			final String wrapperClassName = info.get(MetadataWrapper.METADATA_KEY);

			if (wrapperClassName != null) {
				final Class<?> wrapperClass = ClassUtils.loadClass(wrapperClassName);
				if (wrapperClass == null) {
					log().error("Failed to find class: " + wrapperClassName);
					continue;
				}
				if (wrapperClass.isAssignableFrom(getClass())) {
					final MetadataWrapper metaWrapper =
							getContext().getService(PluginService.class).createInstance(info);
					metaWrapper.wrap(r.getMetadata());
					wrappedMeta = metaWrapper;
					return;
				}
			}
		}

		// No Filter-specific wrapper found
		wrappedMeta = r.getMetadata();
	}

	@Override
	public boolean isCompatible(final Class<?> c) {
		return Reader.class.isAssignableFrom(c);
	}

	// -- Reader API Methods --

	public Plane openPlane(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		openPlaneHelper();
		return getParent().openPlane(imageIndex, planeIndex);
	}

	public Plane openPlane(final int imageIndex, final int planeIndex,
		final int x, final int y, final int w, final int h) throws FormatException,
		IOException
	{
		openPlaneHelper();
		return getParent().openPlane(imageIndex, planeIndex, x, y, w, h);
	}

	public Plane openPlane(final int imageIndex, final int planeIndex,
		final Plane plane) throws FormatException, IOException
	{
		openPlaneHelper();
		return getParent().openPlane(imageIndex, planeIndex, plane);
	}

	public Plane openPlane(final int imageIndex, final int planeIndex,
		final Plane plane, final int x, final int y, final int w, final int h)
		throws FormatException, IOException
	{
		openPlaneHelper();
		return getParent().openPlane(imageIndex, planeIndex, plane, x, y, w, h);
	}

	public Plane openThumbPlane(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		return getParent().openThumbPlane(imageIndex, planeIndex);
	}

	public void setGroupFiles(final boolean group) {
		getParent().setGroupFiles(group);
	}

	public boolean isGroupFiles() {
		return getParent().isGroupFiles();
	}

	public int fileGroupOption(final String id) throws FormatException,
		IOException
	{
		return getParent().fileGroupOption(id);
	}

	public String getCurrentFile() {
		return getParent().getCurrentFile();
	}

	public String[] getDomains() {
		return getParent().getDomains();
	}

	public RandomAccessInputStream getStream() {
		return getParent().getStream();
	}

	public Reader[] getUnderlyingReaders() {
		return getParent().getUnderlyingReaders();
	}

	public int getOptimalTileWidth(final int imageIndex) {
		return getParent().getOptimalTileWidth(imageIndex);
	}

	public int getOptimalTileHeight(final int imageIndex) {
		return getParent().getOptimalTileHeight(imageIndex);
	}

	public void setMetadata(final Metadata meta) throws IOException {
		getParent().setMetadata(meta);

		if (wrappedMeta instanceof MetadataWrapper) ((MetadataWrapper) wrappedMeta)
			.wrap(meta);
		else wrappedMeta = meta;
	}

	public Metadata getMetadata() {
		return wrappedMeta;
	}

	public void setNormalized(final boolean normalize) {
		getParent().setNormalized(normalize);
	}

	public boolean isNormalized() {
		return getParent().isNormalized();
	}

	public boolean hasCompanionFiles() {
		return getParent().hasCompanionFiles();
	}

	public void setSource(final String fileName) throws IOException {
		setSourceHelper(fileName);
		getParent().setSource(fileName);
	}

	public void setSource(final File file) throws IOException {
		setSourceHelper(file.getAbsolutePath());
		getParent().setSource(file);
	}

	public void setSource(final RandomAccessInputStream stream)
		throws IOException
	{
		setSourceHelper(stream.getFileName());
		getParent().setSource(stream);
	}

	public void close(final boolean fileOnly) throws IOException {
		getParent().close(fileOnly);
	}

	public void close() throws IOException {
		getParent().close();
	}

	public Plane readPlane(final RandomAccessInputStream s, final int imageIndex,
		final int x, final int y, final int w, final int h, final Plane plane)
		throws IOException
	{
		readPlaneHelper();
		return getParent().readPlane(s, imageIndex, x, y, w, h, plane);
	}

	public Plane readPlane(final RandomAccessInputStream s, final int imageIndex,
		final int x, final int y, final int w, final int h, final int scanlinePad,
		final Plane plane) throws IOException
	{
		readPlaneHelper();
		return getParent().readPlane(s, imageIndex, x, y, w, h, scanlinePad, plane);
	}

	public int getPlaneCount(final int imageIndex) {
		return getParent().getPlaneCount(imageIndex);
	}

	public int getImageCount() {
		return getParent().getImageCount();
	}

	public Plane createPlane(final int xOffset, final int yOffset,
		final int xLength, final int yLength)
	{
		return getParent().createPlane(xOffset, yOffset, xLength, yLength);
	}

	public Plane createPlane(final ImageMetadata meta, final int xOffset,
		final int yOffset, final int xLength, final int yLength)
	{
		return getParent().createPlane(meta, xOffset, yOffset, xLength, yLength);
	}

	public <P extends Plane> P castToTypedPlane(final Plane plane) {
		return getParent().<P> castToTypedPlane(plane);
	}

	// -- Groupable API Methods --

	public boolean isSingleFile(final String id) throws FormatException,
		IOException
	{
		return getParent().isSingleFile(id);
	}

	// -- HasFormat API Methods --

	public Format getFormat() {
		return getParent().getFormat();
	}

	// -- Contextual API Methods --

	@Override
	public Context getContext() {
		return getParent().getContext();
	}

	@Override
	public void setContext(final Context ctx) {
		getParent().setContext(ctx);
	}

	// -- Helper methods --

	/* Returns true if this filter's metdata can be cast to ChannelFillerMetadata */
	protected boolean metaCheck() {
		final Metadata meta = getMetadata();

		return metaClass.isAssignableFrom(meta.getClass());
	}
}
