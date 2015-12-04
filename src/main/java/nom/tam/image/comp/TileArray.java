package nom.tam.image.comp;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
 * %%
 * This is free and unencumbered software released into the public domain.
 * 
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * #L%
 */

import static nom.tam.fits.header.Compression.COMPRESSED_DATA_COLUMN;
import static nom.tam.fits.header.Compression.GZIP_COMPRESSED_DATA_COLUMN;
import static nom.tam.fits.header.Compression.UNCOMPRESSED_DATA_COLUMN;
import static nom.tam.fits.header.Compression.ZBITPIX;
import static nom.tam.fits.header.Compression.ZBLANK;
import static nom.tam.fits.header.Compression.ZBLANK_COLUMN;
import static nom.tam.fits.header.Compression.ZCMPTYPE;
import static nom.tam.fits.header.Compression.ZNAMEn;
import static nom.tam.fits.header.Compression.ZNAXIS;
import static nom.tam.fits.header.Compression.ZNAXISn;
import static nom.tam.fits.header.Compression.ZQUANTIZ;
import static nom.tam.fits.header.Compression.ZSCALE_COLUMN;
import static nom.tam.fits.header.Compression.ZTILEn;
import static nom.tam.fits.header.Compression.ZVALn;
import static nom.tam.fits.header.Compression.ZZERO_COLUMN;
import static nom.tam.fits.header.Standard.TTYPEn;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardBuilder;
import nom.tam.fits.header.Compression;
import nom.tam.image.comp.ITileCompressorProvider.ITileCompressorControl;
import nom.tam.util.PrimitiveTypeEnum;

/**
 * This class represents a complete array of tiles describing an image ordered
 * from left to right and top down. the tiles all have the same geometry only
 * the tiles at the right side and the bottom side can have differnt sizes.
 */
class TileArray {

    private static final MathContext ROUNDIG_CONTEXT = new MathContext(9, RoundingMode.CEILING);

    private int[] axes;

    /**
     * an enum that interprets the value of the BITPIX keyword in the
     * uncompressed FITS image
     */
    private PrimitiveTypeEnum baseType;

    private int bufferSize;

    /**
     * ZCMPTYPE name of the algorithm that was used to compress
     */
    private String compressAlgorithm;

    private final CompressedImageData compressedImageData;

    private ICompressOption.Parameter[] compressionParameter;

    private ICompressOption[] compressOptions;

    private ITileCompressorControl compressorControl;

    private Buffer decompressedWholeErea;

    private ITileCompressorControl gzipCompressorControl;

    private int naxis;

    /**
     * ZQUANTIZ name of the algorithm that was used to quantize
     */
    private String quantAlgorithm;

    private int[] tileAxes;

    private Tile[] tiles;

    private Integer zblank;

    /**
     * create a TileArray based on a compressed image data.
     *
     * @param compressedImageData
     *            the compressed image data.
     */
    public TileArray(CompressedImageData compressedImageData) {
        this.compressedImageData = compressedImageData;
    }

    public void compress(Header header) throws FitsException {
        executeAllTiles();
        writeHeader(header);
    }

    public ICompressOption[] compressOptions() {
        if (this.compressorControl == null) {
            this.compressorControl = TileCompressorProvider.findCompressorControl(this.quantAlgorithm, this.compressAlgorithm, this.baseType.primitiveClass());
        }
        if (this.gzipCompressorControl == null) {
            this.gzipCompressorControl = TileCompressorProvider.findCompressorControl(null, Compression.ZCMPTYPE_GZIP_1, this.baseType.primitiveClass());
        }
        if (this.compressOptions == null) {
            this.compressOptions = this.compressorControl.options();
        }
        return this.compressOptions;
    }

    private void createTiles(ITileInitialisation init) {
        int nrOfTilesOnXAxis = BigDecimal.valueOf(this.axes[0]).divide(BigDecimal.valueOf(this.tileAxes[0])).round(ROUNDIG_CONTEXT).intValue();
        int nrOfTilesOnYAxis = BigDecimal.valueOf(this.axes[1]).divide(BigDecimal.valueOf(this.tileAxes[1])).round(ROUNDIG_CONTEXT).intValue();
        int lastTileWidth = nrOfTilesOnXAxis * this.tileAxes[0] - this.axes[0];
        if (lastTileWidth == 0) {
            lastTileWidth = this.tileAxes[0];
        }
        int lastTileHeigth = nrOfTilesOnYAxis * this.tileAxes[1] - this.axes[1];
        if (lastTileHeigth == 0) {
            lastTileHeigth = this.tileAxes[1];
        }
        int tileIndex = 0;
        int dataOffset = 0;
        this.tiles = new Tile[nrOfTilesOnXAxis * nrOfTilesOnYAxis];
        for (int y = 0; y < this.axes[1]; y += this.tileAxes[1]) {
            boolean lastY = y + this.tileAxes[1] >= this.axes[1];
            for (int x = 0; x < this.axes[0]; x += this.tileAxes[0]) {
                boolean lastX = x + this.tileAxes[0] >= this.axes[0];
                this.tiles[tileIndex] = init.createTile(tileIndex)//
                        .setDataOffset(dataOffset)//
                        .setWidth(lastX ? lastTileWidth : this.tileAxes[0])//
                        .setHeigth(lastY ? lastTileHeigth : this.tileAxes[1]);
                init.init(this.tiles[tileIndex]);
                dataOffset += this.tiles[tileIndex].getWidth() * this.tiles[tileIndex].getHeigth();
                tileIndex++;
            }
        }
    }

    public Buffer decompress(Buffer decompressed, Header header) {
        int pixels = this.axes[0] * this.axes[1];
        this.decompressedWholeErea = decompressed;
        if (this.decompressedWholeErea == null) {
            this.decompressedWholeErea = this.baseType.newBuffer(pixels);
        }
        for (Tile tile : this.tiles) {
            this.decompressedWholeErea.position(tile.getDataOffset());
            tile.setDecompressedData(this.baseType.sliceBuffer(this.decompressedWholeErea));
        }
        for (ICompressOption option : compressOptions()) {
            option.setCompressionParameter(this.compressionParameter);
        }
        executeAllTiles();
        this.decompressedWholeErea.rewind();
        return this.decompressedWholeErea;
    }

    private void executeAllTiles() {
        ExecutorService threadPool = FitsFactory.threadPool();
        for (Tile tile : this.tiles) {
            tile.execute(threadPool);
        }
        for (Tile tile : this.tiles) {
            tile.waitForResult();
        }
    }

    public PrimitiveTypeEnum getBaseType() {
        return this.baseType;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public ICompressOption[] getCompressOptions() {
        return this.compressOptions;
    }

    public ITileCompressorControl getCompressorControl() {
        return this.compressorControl;
    }

    public ITileCompressorControl getGzipCompressorControl() {
        return this.gzipCompressorControl;
    }

    private <T> T getNullableColumn(Header header, Class<T> class1, String columnName) throws FitsException {
        for (int i = 1; i <= this.compressedImageData.getNCols(); i += 1) {
            String val = header.getStringValue(TTYPEn.n(i));
            if (val != null && val.trim().equals(columnName)) {
                return class1.cast(this.compressedImageData.getColumn(i - 1));
            }
        }
        return null;
    }

    private <T> T getNullableValue(Header header, Class<T> clazz) {
        HeaderCard card = header.findCard(ZBLANK);
        if (card != null) {
            return card.getValue(clazz, null);
        }
        return null;
    }

    public Tile getTile(int i) {
        return this.tiles[i];
    }

    protected TileArray prepareUncompressedData(final Buffer buffer) {
        final ByteBuffer compressed = ByteBuffer.wrap(new byte[this.baseType.size() * this.axes[0] * this.axes[1]]);
        createTiles(new ITileInitialisation() {

            @Override
            public Tile createTile(int tileIndex) {
                return new CompressingTile(TileArray.this, tileIndex);
            }

            @Override
            public void init(Tile tile) {
                buffer.position(tile.getDataOffset());
                tile.setDecompressedData(TileArray.this.baseType.sliceBuffer(buffer));
                compressed.position(tile.getDataOffset() * TileArray.this.baseType.size());
                tile.setCompressedData(compressed.slice());
            }
        });
        return this;
    }

    protected TileArray read(Header header) throws FitsException {
        readStructureInfo(header);
        this.compressAlgorithm = header.getStringValue(ZCMPTYPE);
        this.zblank = getNullableValue(header, Integer.class);
        this.quantAlgorithm = header.getStringValue(ZQUANTIZ);
        readZVALs(header);
        final Object[] compressed = getNullableColumn(header, Object[].class, COMPRESSED_DATA_COLUMN);
        final Object[] uncompressed = getNullableColumn(header, Object[].class, UNCOMPRESSED_DATA_COLUMN);
        final Object[] gzipCompressed = getNullableColumn(header, Object[].class, GZIP_COMPRESSED_DATA_COLUMN);
        final double[] zzero = getNullableColumn(header, double[].class, ZZERO_COLUMN);
        final double[] zscale = getNullableColumn(header, double[].class, ZSCALE_COLUMN);
        final int[] zblankColumn = getNullableColumn(header, int[].class, ZBLANK_COLUMN);

        createTiles(new ITileInitialisation() {

            @Override
            public Tile createTile(int tileIndex) {
                return new DecompressingTile(TileArray.this, tileIndex);
            }

            @Override
            public void init(Tile tile) {
                tile.setCompressed(compressed[tile.getTileIndex()], TileCompressionType.COMPRESSED)//
                        .setCompressed(uncompressed != null ? uncompressed[tile.getTileIndex()] : null, TileCompressionType.UNCOMPRESSED)//
                        .setCompressed(gzipCompressed != null ? gzipCompressed[tile.getTileIndex()] : null, TileCompressionType.GZIP_COMPRESSED)//
                        .setBlank(TileArray.this.zblank != null ? TileArray.this.zblank : zblankColumn == null ? null : zblankColumn[tile.getTileIndex()])//
                        .setZero(zzero == null ? Double.NaN : zzero[tile.getTileIndex()])//
                        .setScale(zscale == null ? Double.NaN : zscale[tile.getTileIndex()]);
            }
        });

        return this;
    }

    protected void readStructureInfo(Header header) throws FitsException {
        this.baseType = PrimitiveTypeEnum.valueOf(header.getIntValue(ZBITPIX));
        this.naxis = header.getIntValue(ZNAXIS);
        this.axes = new int[this.naxis];
        this.bufferSize = 1;
        for (int i = 1; i <= this.naxis; i += 1) {
            int axisValue = header.getIntValue(ZNAXISn.n(i), -1);
            this.axes[i - 1] = axisValue;
            if (this.axes[i - 1] == -1) {
                throw new FitsException("Required ZNAXISn not found");
            }
            this.bufferSize *= axisValue;
        }
        this.tileAxes = new int[this.axes.length];
        Arrays.fill(this.tileAxes, 1);
        this.tileAxes[0] = this.axes[0];
        for (int i = 1; i <= this.naxis; i += 1) {
            HeaderCard card = header.findCard(ZTILEn.n(i));
            if (card != null) {
                this.tileAxes[i - 1] = card.getValue(Integer.class, this.axes[i - 1]);
            }
        }
    }

    private void readZVALs(Header header) {
        int nval = 1;
        HeaderCard card = header.findCard(ZNAMEn.n(nval));
        HeaderCard value;
        while (card != null) {
            card = header.findCard(ZNAMEn.n(++nval));
        }
        this.compressionParameter = new ICompressOption.Parameter[nval--];
        while (nval > 0) {
            card = header.findCard(ZNAMEn.n(nval));
            value = header.findCard(ZVALn.n(nval));
            ICompressOption.Parameter parameter = new ICompressOption.Parameter(card.getValue(), value.getValue(value.valueType(), null));
            this.compressionParameter[--nval] = parameter;
        }
        this.compressionParameter[this.compressionParameter.length - 1] = new ICompressOption.Parameter(Compression.ZQUANTIZ.name(), this.quantAlgorithm);
    }

    public TileArray setAxes(int[] value) {
        this.axes = value;
        return this;
    }

    public TileArray setBaseType(PrimitiveTypeEnum value) {
        this.baseType = value;
        return this;
    }

    public TileArray setBufferSize(int value) {
        this.bufferSize = value;
        return this;
    }

    public TileArray setCompressAlgorithm(String value) {
        this.compressAlgorithm = value;
        return this;
    }

    public TileArray setQuantAlgorithm(String value) {
        this.quantAlgorithm = value;
        return this;
    }

    public TileArray setTileAxes(int[] value) {
        this.tileAxes = value;
        return this;
    }

    private void writeHeader(Header header) throws FitsException {
        HeaderCardBuilder cardBilder = header.card(ZBITPIX);
        cardBilder.value(this.baseType.bitPix())//
                .card(ZCMPTYPE).value(this.compressAlgorithm);
        if (this.zblank != null) {
            cardBilder.card(ZBLANK).value(this.zblank);
        }
        if (this.quantAlgorithm != null) {
            cardBilder.card(ZQUANTIZ).value(this.quantAlgorithm);
        }
        boolean compressedColumn = false;
        boolean gzipColumn = false;
        boolean uncompressedColumn = false;
        boolean zeroColumn = false;
        boolean scaleColumn = false;
        boolean blankColumn = false;
        for (Tile tile : this.tiles) {
            compressedColumn = compressedColumn || tile.getCompressionType() == TileCompressionType.COMPRESSED;
            gzipColumn = gzipColumn || tile.getCompressionType() == TileCompressionType.GZIP_COMPRESSED;
            uncompressedColumn = uncompressedColumn || tile.getCompressionType() == TileCompressionType.UNCOMPRESSED;
            blankColumn = blankColumn || tile.getBlank() != null;
            zeroColumn = zeroColumn || !Double.isNaN(tile.getZero());
            scaleColumn = scaleColumn || Double.isNaN(tile.getScale());
        }
    }
}
