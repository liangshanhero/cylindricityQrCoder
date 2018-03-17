package com.hombio.util;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
public class QRCoder {

	private static class Point{
		private float x;
		private float y;
		
		public float getX() {
			return x;
		}
		public void setX(float x) {
			this.x = x;
		}
		public float getY() {
			return y;
		}
		public void setY(float y) {
			this.y = y;
		}
	}

	private static BitMatrix createBitMatrix(String content, int size) throws WriterException {
		Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();

		// 设置纠错等级
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		// 设置字符集
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		// 设置外边距
		hints.put(EncodeHintType.MARGIN, 0);
		// 二维条码的区域
		// 获得编码后的位矩阵
		return new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);
	}
	private static  List<Point> getPointList(BitMatrix bitMatrix){
		List<Point> pointList = new ArrayList<Point>();
		for(int i=0 ; i<bitMatrix.getWidth(); i++) {
			for(int j=0; j<bitMatrix.getHeight(); j++) {
				if(bitMatrix.get(i,j)) {
					Point point = new Point();
					point.setX(i);
					point.setY(j);
					pointList.add(point);
				}
			}
		}
		return pointList;
	}

	private static List<Point> transformPointToCylinderVertical(BitMatrix bitMatrix, float r, float l,float centreLineX,float centreLineY){

		List<Point> pointList = getPointList(bitMatrix);
		for(int i=0; i<pointList.size();i++) {
			float y = pointList.get(i).getY();
			y += centreLineY - centreLineX;
			y = centreLineY-y;
			float newY = caculateArcLength(r, l, Math.abs(y));
			if(y<0) {
				newY = 0 -newY;
			}
			pointList.get(i).setY(centreLineY - newY);
		}
		return pointList;
	}
	
	private static List<Point> transformPointToCylinderHorizontal(BitMatrix bitMatrix, float r, float l,float centreLineX,float centreLineY){
		List<Point> pointList = getPointList(bitMatrix);
		for(int i=0; i<pointList.size();i++) {
			float x = pointList.get(i).getX();
			x += centreLineX - centreLineY;
			x -= centreLineX;
			float newX = caculateArcLength(r, l, Math.abs(x));
			if(x<0) {
				newX = 0 - newX;
			}
			pointList.get(i).setX(newX+centreLineX);
		}
		return pointList;
	}

	private static List<Point> transformPointToGlobe(BitMatrix bitMatrix, float r, float l,float centreLineX,float centreLineY,float increX, float IncreY){
		List<Point> pointList = getPointList(bitMatrix);
		for(int i=0; i<pointList.size();i++) {
			float x = pointList.get(i).getX();
			float y = pointList.get(i).getY();
			x += increX;
			y += IncreY;
			x -= centreLineX;
			y = centreLineY - y;
			x = caculateIncreRateOnGlobe(r, l, x, y) * x;
			y = caculateIncreRateOnGlobe(r, l, x, y) * y;
			x += centreLineX;
			y = centreLineY - y;

			pointList.get(i).setX(x);
			pointList.get(i).setY(y);
		}
		return pointList;
	}
	
	private static BufferedImage createQRImageCylinderVertical(BitMatrix bitMatrix,float r, float l, int size) {
		float halfHeight = new BigDecimal(size / 2.0).floatValue();
		float newHalfHeight = caculateArcLength(r, l,halfHeight);
		BufferedImage bufferedImage = new BufferedImage(bitMatrix.getWidth(), Math.round(newHalfHeight*2), BufferedImage.TYPE_INT_RGB);
		List<Point> pointList = transformPointToCylinderVertical(bitMatrix, r, l, halfHeight,newHalfHeight);
		Graphics2D graphics2d = bufferedImage.createGraphics();
		graphics2d.setColor(Color.white);
		graphics2d.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
		graphics2d.setColor(Color.BLACK);
		for(Point point : pointList) {
			graphics2d.drawString(".", point.getX(),point.getY());
		}
		graphics2d.dispose();
		return bufferedImage;
	}
	private static BufferedImage createQRImageCylinderHorizontal(BitMatrix bitMatrix,float r, float l, int size) {
		float halfWidth = new BigDecimal(bitMatrix.getWidth()/2.0).floatValue();
		float newHalfWidth = caculateArcLength(r, l, halfWidth);
		BufferedImage bufferedImage = new BufferedImage(Math.round(newHalfWidth*2), bitMatrix.getHeight(), BufferedImage.TYPE_INT_RGB);
		
		List<Point> pointList = transformPointToCylinderHorizontal(bitMatrix, r, l,
				newHalfWidth,new BigDecimal(bitMatrix.getHeight()/2.0).floatValue());
		
		Graphics2D graphics2d = bufferedImage.createGraphics();
		graphics2d.setColor(Color.white);
		graphics2d.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
		graphics2d.setColor(Color.BLACK);
		for(Point point : pointList) {
			graphics2d.drawString(".", point.getX(),point.getY());
		}
		graphics2d.dispose();
		return bufferedImage;
	}
	private static BufferedImage createGlobeQRImage(BitMatrix bitMatrix,float r, float l, int size) {
		float halfWidth = new BigDecimal(size/2.0).floatValue();
		float halfHeight = new BigDecimal(size/2.0).floatValue();
		float newHalfWidth = caculateIncreRateOnGlobe(r, l, halfWidth, halfHeight) * halfWidth ;
		float newHalfHeight = caculateIncreRateOnGlobe(r, l, halfWidth, halfHeight) * halfHeight ;
		BufferedImage bufferedImage = new BufferedImage(Math.round(newHalfWidth*2),Math.round(newHalfHeight*2) , BufferedImage.TYPE_INT_RGB);
		
		List<Point> pointList = transformPointToGlobe(bitMatrix, r, l, newHalfWidth, newHalfHeight, newHalfWidth - halfWidth, newHalfHeight-halfHeight);
		
		Graphics2D graphics2d = bufferedImage.createGraphics();
		graphics2d.setColor(Color.white);
		graphics2d.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
		graphics2d.setColor(Color.BLACK);
		for(Point point : pointList) {
			graphics2d.drawString(".", point.getX(), point.getY());
		}
		graphics2d.dispose();
		return bufferedImage;
	}
	
	public static float caculateIncreRateOnGlobe(float r, float l, float x,float y) {
		double a = Math.sqrt(x*x+y*y);
		double b = 1 + l/r;
		double c = Math.atan(a/l);
		double d = (Math.asin(b * Math.sin(c)) - c)*r/a;
		return new BigDecimal(d).floatValue();
	}
	
	public static float caculateArcLength(float r, float l, float a) {
		double x = Math.atan(a/l);
		double temp = 1+l/r; 
		double y = Math.PI - Math.asin( temp * Math.sin(x));
		double z = Math.PI - x - y;
		double newA =  z*r;
		BigDecimal bigDecimal = new BigDecimal(newA);
		return bigDecimal.floatValue();
	}
	
	private static BufferedImage createQRImage(BitMatrix bitMatrix ) throws Exception {
		int size = bitMatrix.getWidth();
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2d = image.createGraphics();
		graphics2d.setColor(Color.white);
		graphics2d.fillRect(0, 0, image.getWidth(), image.getHeight());
		graphics2d.setColor(Color.BLACK);
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				if(bitMatrix.get(x, y)){
					graphics2d.drawString(".", x, y);
				}
			}
		}
		graphics2d.dispose();
		return image;
	}
	
	
//	在生成的二维条码下面插入备注信息
	private static BufferedImage appendMemoUnderQR(BufferedImage qrImage,String memo) throws Exception {
		if(memo==null || memo.length()==0) {
			return qrImage ;
		}
		int width = qrImage.getWidth();
		int height = qrImage.getHeight();
		int increHeight = Math.round(width * (44.0F/223));
		Image image = qrImage.getScaledInstance(width, height, Image.SCALE_DEFAULT);
		BufferedImage bufferedImage = new BufferedImage(width, height+increHeight, BufferedImage.TYPE_INT_RGB);

		float memoX = width/20;	
	    float memoY = height+increHeight/2;;
	    
		Graphics2D graphics2D = bufferedImage.createGraphics();
		graphics2D.setColor(Color.WHITE);
		graphics2D.fillRect(0, 0, width, height+increHeight);
		graphics2D.drawImage(image, 0, 0, width, height, null);
		graphics2D.setColor(Color.black);
		graphics2D.setFont(new Font(null, Font.BOLD, Math.round(0.08F*width)));
		graphics2D.drawString(memo,  memoX,	 memoY);
		graphics2D.dispose();
		return bufferedImage;
	}
	
	
////	在生成的二维条码中间插入图片
//	private static void insertImageIntoQR(BufferedImage sourceBufferedImage, File logImgFile, boolean needCompress) throws Exception {
////		File file = new File(logImg);
//		if (!logImgFile.exists()) {
//			System.err.println(""+logImgFile+"   该文件不存在！");
//			return;
//		}
//		Image src = ImageIO.read(logImgFile);
//		int width = src.getWidth(null);
//		int height = src.getHeight(null);
//		if (needCompress) { // 压缩LOGO
//			if (width > logoWidth) {
//				width = logoWidth;
//			}
//			if (height > logoHeight) {
//				height = logoHeight;
//			}
//			Image image = src.getScaledInstance(width, height,	Image.SCALE_SMOOTH);
//			BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//			Graphics g = tag.getGraphics();
//			g.drawImage(image, 0, 0, null); // 绘制缩小后的图
//			g.dispose();
//			src = image;
//		}
//		// 插入LOGO
//		Graphics2D graph = sourceBufferedImage.createGraphics();
//		int x = (codeWidth - width) / 2;
//		int y = (codeWidth - height) / 2;
//		graph.drawImage(src, x, y, width, height, null);
//		Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
//		graph.draw(shape);
//		graph.dispose();
//	}


//	private static void mkdirs(String destPath) {
//		File file =new File(destPath);    
//		//当文件夹不存在时，mkdirs会自动创建多层目录，区别于mkdir．(mkdir如果父目录不存在则会抛出异常)
//		if (!file.exists() && !file.isDirectory()) {
//			file.mkdirs();
//		}
//	}


	public static  BufferedImage encode(String qrContent, int size) throws Exception {
		return createQRImage(createBitMatrix(qrContent, size));
	}
	
	public static BufferedImage[] encode(String qrContent, String memo, int side, float r, float l,boolean  isVertical) throws IOException, Exception {
		BufferedImage[] qrImageArray = new BufferedImage[2];
		int size = (int) Math.round(side*55.74);
		r = new BigDecimal(r*55.74).floatValue();
		l = new BigDecimal(l*55.74).floatValue();
		BitMatrix bitMatrix = createBitMatrix(qrContent, size);
		BufferedImage qrImage = createQRImage(bitMatrix);
		qrImage = appendMemoUnderQR(qrImage, memo);
		qrImageArray[0] = qrImage;
		BufferedImage qrImage1 = null;
		if(isVertical) {
			qrImage1 = createQRImageCylinderVertical(bitMatrix, r, l, size);
		}else {
			qrImage1 = createQRImageCylinderHorizontal(bitMatrix, r, l, size);
		}
		qrImage1 = appendMemoUnderQR(qrImage1, memo);
		qrImageArray[1] = qrImage1;
		return qrImageArray;
	}
	
	public static BufferedImage[] encode(String qrContent, String memo, int side, float r, float l) throws IOException, Exception {
		BufferedImage[] qrImageArray = new BufferedImage[2];
		int size = (int) Math.round(side*55.74);
		r = new BigDecimal(r*55.74).floatValue();
		l = new BigDecimal(l*55.74).floatValue();
		BitMatrix bitMatrix = createBitMatrix(qrContent, size);
		BufferedImage qrImage = createQRImage(bitMatrix);
		qrImage = appendMemoUnderQR(qrImage, memo);
		qrImageArray[0] = qrImage;
		BufferedImage qrImage1 = createGlobeQRImage(bitMatrix, r, l, size);
		qrImage1 = appendMemoUnderQR(qrImage1, memo);
		qrImageArray[1] = qrImage1;
		return qrImageArray;
	}
	
	public static void saveImage(BufferedImage bufferedImage, File targetpath, String imageFormat) throws Exception {
		ImageIO.write(bufferedImage, imageFormat, targetpath);
	}

}
