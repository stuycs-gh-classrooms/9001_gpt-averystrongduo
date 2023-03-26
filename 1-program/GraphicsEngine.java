import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GraphicsEngine {

    private BufferedImage image;
    private Color color;
    private int width;
    private int height;
    private int[] pixels;

    public GraphicsEngine(int width, int height) {
        this.width = width;
        this.height = height;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.color = Color.WHITE;
        this.pixels = image.getRGB(0, 0, width, height, null, 0, width);
    }

    public void clear() {
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = Color.BLACK.getRGB();
        }
    }

    public void setPixel(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            pixels[y * width + x] = color.getRGB();
        }
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void drawLine(int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        while (x0 != x1 || y0 != y1) {
            setPixel(x0, y0);
            int e2 = err * 2;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
        setPixel(x1, y1);
    }

    public void drawCircle(int xc, int yc, int r) {
        int x = 0;
        int y = r;
        int d = 3 - 2 * r;
        while (x <= y) {
            setPixel(xc + x, yc + y);
            setPixel(xc + x, yc - y);
            setPixel(xc - x, yc + y);
            setPixel(xc - x, yc - y);
            setPixel(xc + y, yc + x);
            setPixel(xc + y, yc - x);
            setPixel(xc - y, yc + x);
            setPixel(xc - y, yc - x);
            x++;
            if (d < 0) {
                d += 4 * x + 6;
            } else {
                d += 4 * (x - y) + 10;
                y--;
            }
        }
    }

    public void drawBezierCurve(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3) {
        for (double t = 0; t <= 1; t += 0.001) {
            int x = (int) (Math.pow(1 - t, 3) * x0 + 3 * t * Math.pow(1 - t, 2) * x2 + Math.pow(t, 3) * x3);
            int y = (int) (Math.pow(1 - t, 3) * y0 + 3 * t * Math.pow(1 - t, 2) * y1 + 3 * Math.pow(t, 2) * (1 - t) * y2
                    + Math.pow(t, 3) * y3);
            setPixel(x, y);
        }
    }

    public void drawHermiteCurve(int x0, int y0, int x1, int y1, int rx0, int ry0, int rx1, int ry1) {
        double t0 = 0;
        double t1 = 1;
        double step = 0.001;
        double dt = t1 - t0;
        int x, y;
        for (double t = t0; t <= t1; t += step) {
            double h1 = 2 * Math.pow(t, 3) - 3 * Math.pow(t, 2) + 1;
            double h2 = -2 * Math.pow(t, 3) + 3 * Math.pow(t, 2);
            double h3 = Math.pow(t, 3) - 2 * Math.pow(t, 2) + t;
            double h4 = Math.pow(t, 3) - Math.pow(t, 2);
            x = (int) (h1 * x0 + h2 * x1 + h3 * rx0 + h4 * rx1);
            y = (int) (h1 * y0 + h2 * y1 + h3 * ry0 + h4 * ry1);
            setPixel(x, y);
        }
    }

    public void translate(int dx, int dy) {
        int[] newPixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int newX = x - dx;
                int newY = y - dy;
                if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                    newPixels[y * width + x] = pixels[newY * width + newX];
                }
            }
        }
        pixels = newPixels;
    }

    public void rotate(double angle) {
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        int[] newPixels = new int[width * height];
        int cx = width / 2;
        int cy = height / 2;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int newX = (int) ((x - cx) * cos - (y - cy) * sin + cx);
                int newY = (int) ((x - cx) * sin + (y - cy) * cos + cy);
                if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                    newPixels[y * width + x] = pixels[newY * width + newX];
                }
            }
        }
        pixels = newPixels;
    }

    public void dilate(double scale) {
        int[] newPixels = new int[width * height];
        int cx = width / 2;
        int cy = height / 2;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int newX = (int) ((x - cx) * scale + cx);
                int newY = (int) ((y - cy) * scale + cy);
                if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                    newPixels[y * width + x] = pixels[newY * width + newX];
                }
            }
        }
        pixels = newPixels;
    }
    public void saveAsPPM(String filename) {
        try {
            PrintWriter writer = new PrintWriter(filename, "UTF-8");
            writer.println("P3");
            writer.println(width + " " + height);
            writer.println("255");
            for (int i = 0; i < pixels.length; i++) {
                writer.print(getRed(pixels[i]) + " ");
                writer.print(getGreen(pixels[i]) + " ");
                writer.println(getBlue(pixels[i]));
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }
    
    private int getRed(int color) {
        return (color >> 16) & 0xFF;
    }
    
    private int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }
    
    private int getBlue(int color) {
        return color & 0xFF;
    }
    
    private int getRGB(int red, int green, int blue) {
        return (red << 16) | (green << 8) | blue;
    }
    
    public static void main(String[] args) {
        // Create a new GraphicsEngine with a canvas size of 500x500 pixels
        GraphicsEngine engine = new GraphicsEngine(500, 500);
    
        // Draw a red line from (100, 100) to (400, 400)
        engine.setColor(Color.RED);
        engine.drawLine(100, 100, 400, 400);
    
        // Draw a green circle with radius 50 centered at (250, 250)
        engine.setColor(Color.GREEN);
        engine.drawCircle(250, 250, 50);
    
        // Draw a blue Bezier curve with control points at (100, 400), (200, 100), (300, 400), (400, 100)
        engine.setColor(Color.BLUE);
        engine.drawBezierCurve(100, 400, 200, 100, 300, 400, 400, 100);
    
        // Draw a yellow Hermite curve with start point (50, 50), end point (450, 450), tangent vectors (1, 0) and (0, 1)
        engine.setColor(Color.YELLOW);
        engine.drawHermiteCurve(50, 50, 1, 0, 450, 450, 0, 1);
    
        // Rotate the canvas 45 degrees around the center point (250, 250)
        engine.rotate(Math.toRadians(45));
    
        // Dilate the canvas by a factor of 2
        engine.dilate(2);
    
        // Translate the canvas by (50, 50)
        engine.translate(50, 50);
    
        // Save the image as a PPM file called "test.ppm"
        engine.saveAsPPM("test.ppm");
    }
    
    
    
    
    
}    