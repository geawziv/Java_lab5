package lab4v2;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
	// Список координат точек для построения графика
    private Double[][] graphicsData;
    // Флаговые переменные, задающие правила отображения графика
    private boolean showAxis = true;
    private boolean showMarkers = true;
    // Границы диапазона пространства, подлежащего отображению
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    // Используемый масштаб отображения
    private double scaleX;
    private double scaleY;
    private boolean isDragging = false;
    private Point dragStart = null;
    private Rectangle dragRect = null;
    // Различные стили черчения линий
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    // Различные шрифты отображения надписей
    private Font axisFont;

    public GraphicsDisplay() {
    	// Цвет заднего фона области отображения - белый
    	        setBackground(Color.WHITE);
    	// Сконструировать необходимые объекты, используемые в рисовании
    	// Перо для рисования графика
    	        float[] dash = {3,10,12,10,3,10,21,10,12,10,3,10};
    	        graphicsStroke = new BasicStroke(4.0f, BasicStroke.CAP_SQUARE,
    	                BasicStroke.JOIN_MITER, 22.0f, dash, 0.0f);
    	// Перо для рисования осей координат
    	        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
    	                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
    	// Перо для рисования контуров маркеров
    	        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
    	                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
    	// Шрифт для подписей осей координат
    	        axisFont = new Font("Serif", Font.BOLD, 36);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    resetScale(); // Восстановление исходного масштаба
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    isDragging = true; // Начало выделения
                    dragStart = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging) {
                    if (dragRect != null && dragRect.width > 0 && dragRect.height > 0) {
                        scaleToArea(dragRect); // Масштабирование выделенной области
                    }
                    isDragging = false;
                    dragRect = null;
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
            	 showPointCoordinates(e); // Отображение координат точки при наведении
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    dragRect = new Rectangle(dragStart);
                    dragRect.add(e.getPoint()); // Рисование рамки выделения
                    repaint();
                }
            }
        });
    }

    private void showPointCoordinates(MouseEvent e) {
        if (graphicsData == null) return;

        Point mousePoint = e.getPoint();
        for (Double[] point : graphicsData) {
            Point2D.Double graphPoint = xyToPoint(point[0], point[1]);
            if (Math.abs(graphPoint.x - mousePoint.x) < 5 && Math.abs(graphPoint.y - mousePoint.y) < 5) {
                Graphics2D g2d = (Graphics2D) getGraphics();
                g2d.setColor(Color.BLACK);
                g2d.drawString(String.format("(%.2f, %.2f)", point[0], point[1]),
                        (int) graphPoint.x + 5, (int) graphPoint.y - 5);
                break;
            }
        }
    }

    private void scaleToArea(Rectangle rect) {
        double newMinX = minX + (rect.x / scaleX);
        double newMaxX = minX + ((rect.x + rect.width) / scaleX);
        double newMinY = maxY - ((rect.y + rect.height) / scaleY);
        double newMaxY = maxY - (rect.y / scaleY);

        minX = newMinX;
        maxX = newMaxX;
        minY = newMinY;
        maxY = newMaxY;

        scaleX = getWidth() / (maxX - minX);
        scaleY = getHeight() / (maxY - minY);

        repaint();
    }

    private void resetScale() {
        calculateBounds(); // Восстановление границ
        repaint();
    }

    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;
        calculateBounds();
        repaint();
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }
    
    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX * scaleX, deltaY * scaleY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graphicsData == null || graphicsData.length == 0) return;

        Graphics2D canvas = (Graphics2D) g;
        if (showAxis) paintAxis(canvas);
        paintGraphics(canvas);
        if (showMarkers) paintMarkers(canvas);

        if (dragRect != null) { // Рисование рамки выделения
            canvas.setColor(Color.BLACK);
            canvas.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL, 0, new float[]{6, 6}, 0));
            canvas.draw(dragRect);
        }
    }

    private void calculateBounds() {
        minX = graphicsData[0][0];
        maxX = graphicsData[0][0];
        minY = graphicsData[0][1];
        maxY = graphicsData[0][1];

        for (Double[] point : graphicsData) {
            if (point[0] < minX) minX = point[0];
            if (point[0] > maxX) maxX = point[0];
            if (point[1] < minY) minY = point[1];
            if (point[1] > maxY) maxY = point[1];
        }

        maxX += maxX * 0.25;
        minX -= maxX * 0.25;
        maxY += maxX * 0.2;
        minY -= maxX * 0.1;

        scaleX = getWidth() / (maxX - minX);
        scaleY = getHeight() / (maxY - minY);
    }

    protected void paintAxis(Graphics2D canvas) {
        canvas.setStroke(new BasicStroke(2.0f));
        canvas.setColor(Color.BLACK);

        Point2D.Double xStart = xyToPoint(minX, 0);
        Point2D.Double xEnd = xyToPoint(maxX, 0);
        canvas.draw(new Line2D.Double(xStart, xEnd));

        Point2D.Double yStart = xyToPoint(0, minY);
        Point2D.Double yEnd = xyToPoint(0, maxY);
        canvas.draw(new Line2D.Double(yStart, yEnd));
    }

    protected void paintGraphics(Graphics2D canvas) {
    	// Выбрать линию для рисования графика
    	        canvas.setStroke(graphicsStroke);
    	// Выбрать цвет линии
    	        canvas.setColor(Color.GRAY);
    	/* Будем рисовать линию графика как путь, состоящий из множества
    	сегментов (GeneralPath)
    	* Начало пути устанавливается в первую точку графика, после чего
    	прямой соединяется со
    	* следующими точками
    	*/
    	        GeneralPath graphics = new GeneralPath();
    	        for (int i=0; i<graphicsData.length; i++) {
    	// Преобразовать значения (x,y) в точку на экране point
    	            Point2D.Double point = xyToPoint(graphicsData[i][0],
    	                    graphicsData[i][1]);
    	            if (i>0) {
    	// Не первая итерация цикла - вести линию в точку                point
    	                graphics.lineTo(point.getX(), point.getY());
    	            } else {
    	// Первая итерация цикла - установить начало пути в                точку point
    	                graphics.moveTo(point.getX(), point.getY());
    	            }
    	        }
    	// Отобразить график
    	        canvas.draw(graphics);
    	    }

    protected boolean hasOnlyEvenDigits(double value) {
        // Получаем целую часть числа
        int integerPart = (int) Math.abs(value);

        // Преобразуем число в строку для анализа цифр
        String numStr = String.valueOf(integerPart);

        // Проверяем каждую цифру
        for (int i = 0; i < numStr.length(); i++) {
            int digit = Character.getNumericValue(numStr.charAt(i));
            if (digit % 2 != 0) { // Если цифра нечётная
                return false;
            }
        }
        return true;
    }

    // Отображение маркеров точек, по которым рисовался график
    protected void paintMarkers(Graphics2D canvas) {
        // Установить более толстое перо для четкого контура
        canvas.setStroke(new BasicStroke(2.0f));

        // Шаг 2 - Организовать цикл по всем точкам графика
        for (Double[] point: graphicsData) {
            Point2D.Double center = xyToPoint(point[0], point[1]);

            // Выбор цвета в зависимости от значения точки
            if (hasOnlyEvenDigits(point[1])) {
                canvas.setColor(Color.BLUE);
            } else {
                canvas.setColor(Color.BLACK);
            }

            // Создаем четырехконечную звезду
            int size = 8; // Размер звезды
            int innerSize = 3; // Внутренний размер

            int[] xPoints = new int[8];
            int[] yPoints = new int[8];

            // Верхний луч
            xPoints[0] = (int)center.x;
            yPoints[0] = (int)(center.y - size);
            xPoints[1] = (int)(center.x + innerSize);
            yPoints[1] = (int)(center.y - innerSize);

            // Правый луч
            xPoints[2] = (int)(center.x + size);
            yPoints[2] = (int)center.y;
            xPoints[3] = (int)(center.x + innerSize);
            yPoints[3] = (int)(center.y + innerSize);

            // Нижний луч
            xPoints[4] = (int)center.x;
            yPoints[4] = (int)(center.y + size);
            xPoints[5] = (int)(center.x - innerSize);
            yPoints[5] = (int)(center.y + innerSize);

            // Левый луч
            xPoints[6] = (int)(center.x - size);
            yPoints[6] = (int)center.y;
            xPoints[7] = (int)(center.x - innerSize);
            yPoints[7] = (int)(center.y - innerSize);

            // Рисуем только контур звезды
            canvas.draw(new Polygon(xPoints, yPoints, 8));
        }
    }

}