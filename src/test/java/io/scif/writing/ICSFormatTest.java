/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2018 SCIFIO developers.
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
 * #L%
 */
package io.scif.writing;

import io.scif.img.IO;
import io.scif.io.location.TestImgLocation;
import java.io.IOException;

import net.imagej.ImgPlus;
import org.junit.Test;

public class ICSFormatTest extends AbstractSyntheticWriterTest {

	public ICSFormatTest() {
		super(".ics");
	}

	@Test
	public void testWriting_uint8() throws IOException {
		final ImgPlus<?> sourceImg = IO.open(new TestImgLocation.Builder().name(
			"testimg").pixelType("uint8").axes("X", "Y", "C").lengths(100, 100, 3)
			.build()).get(0);
		testWriting(sourceImg);

		final ImgPlus<?> sourceImg2 = IO.open(new TestImgLocation.Builder().name(
			"testimg").pixelType("uint8").axes("X", "Y", "C", "Time").lengths(100,
				100, 3, 3).build()).get(0);
		testWriting(sourceImg2);

		final ImgPlus<?> sourceImg3 = IO.open(new TestImgLocation.Builder().name(
			"testimg").pixelType("uint8").axes("X", "Y", "Channel", "Z", "Time")
			.lengths(100, 100, 3, 10, 13).build()).get(0);
		testWriting(sourceImg3);

		final ImgPlus<?> sourceImg4 = IO.open(new TestImgLocation.Builder().name(
			"testimg").pixelType("uint8").axes("X", "Y", "C", "Z", "T").lengths(100,
				100, 3, 3, 3).build()).get(0);
		testWriting(sourceImg4);

		final ImgPlus<?> sourceImg5 = IO.open(new TestImgLocation.Builder().name(
			"testimg").pixelType("uint8").axes("X", "Y", "Z", "Custom").lengths(100,
				100, 3, 3).build()).get(0);
		testWriting(sourceImg5);

		final ImgPlus<?> sourceImg6 = IO.open(new TestImgLocation.Builder().name(
			"testimg").pixelType("uint8").axes("X", "Y").lengths(100, 100).build())
			.get(0);
		testWriting(sourceImg6);
	}

	@Test
	public void testWriting_int8() throws IOException {
		final ImgPlus<?> sourceImg = IO.open(new TestImgLocation.Builder().name(
			"testimg").pixelType("int8").axes("X", "Y", "C").lengths(100, 100, 3)
			.build()).get(0);
		testWriting(sourceImg);
	}

	@Test
	public void testWriting_int16() throws IOException {

		final ImgPlus<?> sourceImg = IO.open(new TestImgLocation.Builder().name(
			"testimg").pixelType("int16").axes("X", "Y", "C").lengths(100, 100, 3)
			.build()).get(0);
		testWriting(sourceImg);
	}

	@Test
	public void testWriting_uint16() throws IOException {

		final ImgPlus<?> sourceImg = IO.open(new TestImgLocation.Builder().name(
			"testimg").pixelType("uint16").axes("X", "Y", "C").lengths(100, 100, 3)
			.build()).get(0);
		testWriting(sourceImg);
	}

	@Test
	public void testWriting_int32() throws IOException {

		final ImgPlus<?> sourceImg = IO.open(new TestImgLocation.Builder().name(
			"testimg").pixelType("int32").axes("X", "Y", "C").lengths(100, 100, 3)
			.build()).get(0);
		testWriting(sourceImg);
	}

	@Test
	public void testWriting_uint32() throws IOException {

		final ImgPlus<?> sourceImg = IO.open(new TestImgLocation.Builder().name(
			"testimg").pixelType("uint32").axes("X", "Y", "C").lengths(100, 100, 3)
			.build()).get(0);
		testWriting(sourceImg);
	}

	@Test
	public void testWriting_float() throws IOException {
		final ImgPlus<?> sourceImg = IO.open(new TestImgLocation.Builder().name(
			"testimg").pixelType("float").axes("X", "Y", "C").lengths(100, 100, 3)
			.build()).get(0);
		testWriting(sourceImg);
	}
}
