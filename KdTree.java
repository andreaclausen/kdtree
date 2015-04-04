public class KdTree {
    
    private static final boolean VERTICAL = true;
    private static final boolean HORIZONTAL = false;
    
    private Node root;  // root of 2d tree
    private int size;   // number of points in the tree
    
    // construct an empty set of points
    public KdTree() {
        size = 0;  // 0 points in the tree initially
    }                               
    
    private static class Node {
        private Point2D p;      // the point
        private RectHV rect;    // the axis-aligned rectangle corresponding to this node
        private Node lb;        // the left/bottom subtree
        private Node rt;        // the right/top subtree
        
        public Node(Point2D p, RectHV rect) {
            this.p = p;
            this.rect = rect;
        }
    }
    
    // is the set empty? 
    public boolean isEmpty() {
        return size() == 0;
    }                   
    
    // number of points in the set 
    public int size() {
        return size;
    }                        
    
    /***********************************************************************
    *  Insert point into 2d tree if it is not already in the set
    ***********************************************************************/
    
    // add the point to the set (if it is not already in the set)
    public void insert(Point2D p) {
        root = insert(root, p, VERTICAL, 0, 0, 1, 1);
    }
    
    private Node insert(Node x, Point2D p, boolean orientation,
                        double xmin, double ymin, double xmax, double ymax) {
        if (x == null) {
            this.size++;
            return new Node(p, new RectHV(xmin, ymin, xmax, ymax));
        }
        
        if (x.p.equals(p)) return x;  // do not insert duplicate points
        
        if (orientation == VERTICAL) {
            int cmp = xcompare(p, x.p);
            if (cmp < 0) {
                x.lb = insert(x.lb, p, !orientation,
                              x.rect.xmin(), x.rect.ymin(), x.p.x(), x.rect.ymax());  // insert left points left
            }
            else {
                x.rt = insert(x.rt, p, !orientation,
                              x.p.x(), x.rect.ymin(), x.rect.xmax(), x.rect.ymax());  // insert equal or right points right
            }
        }
        else {
            int cmp = ycompare(p, x.p);
            if (cmp < 0) {
                x.lb = insert(x.lb, p, !orientation,
                              x.rect.xmin(), x.rect.ymin(), x.rect.xmax(), x.p.y());  // insert below points left
            }
            else {         
                x.rt = insert(x.rt, p, !orientation,
                              x.rect.xmin(), x.p.y(), x.rect.xmax(), x.rect.ymax());  // insert equal or above points right
            }
        }
        return x;
    }
    
    /***********************************************************************
    *  Search 2d tree for given point, return null if not found
    ***********************************************************************/
    
    // does the set contain point p?
    public boolean contains(Point2D p) {
        return get(p) != null;
    }             
    
    private Point2D get(Point2D p) {
        return get(root, p, VERTICAL);  // returns null if point not found
    }
    
    private Point2D get(Node x, Point2D p, boolean orientation) {
        if      (x == null) return null;
        else if (x.p.equals(p))  return p;
        else if (orientation == VERTICAL) {
            int cmp = xcompare(p, x.p);
            if (cmp < 0)      return get(x.lb, p, !orientation);
            else              return get(x.rt, p, !orientation);
        }
        else {
            int cmp = ycompare(p, x.p);
            if (cmp < 0)      return get(x.lb, p, !orientation);
            else              return get(x.rt, p, !orientation);
        }
    }
    
    /***********************************************************************
    *  Draw rectangles
    ***********************************************************************/
    
    // draw all points to standard draw
    public void draw() {
        draw(root, VERTICAL);
    }

    private void draw(Node x, boolean orientation) {
        
        //draw splitting lines
        if (orientation == VERTICAL) {
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.setPenRadius();
            StdDraw.line(x.p.x(), x.rect.ymin(), x.p.x(), x.rect.ymax());
        }
        else {
            StdDraw.setPenColor(StdDraw.BLUE);
            StdDraw.setPenRadius();
            StdDraw.line(x.rect.xmin(), x.p.y(), x.rect.xmax(), x.p.y());
        }

        if (x.lb != null) {
            draw(x.lb, !orientation);
        }

        if (x.rt != null) {
            draw(x.rt, !orientation);
        }

        // draw point
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setPenRadius(.01);
        x.p.draw();
    }                       
    
    /***********************************************************************
    *  Range search
    ***********************************************************************/
    
    // all points that are inside the rectangle
    public Iterable<Point2D> range(RectHV rect) {
        
        Stack<Point2D> s = new Stack<Point2D>();
        range(root, rect, s);
        return s;
    }             
   
    private void range(Node x, RectHV rect, Stack<Point2D> s) {
        if (x == null)                return;  // reached a null node
        if (!x.rect.intersects(rect)) return;  // doesn't intersect rectangle corresponding to this node
        if (rect.contains(x.p))       s.push(x.p);
        
        // recursively search the left and right subtrees
        range(x.lb, rect, s);
        range(x.rt, rect, s);
    }
    
    /***********************************************************************
    *  Nearest neigbors
    ***********************************************************************/
    
    // a nearest neighbor in the set to point p; null if the set is empty
    public Point2D nearest(Point2D p) {
        return nearest(root, p, 2.0);  // assuming 1x1 coordinate system, maximum squared distance = 2
    }

    private Point2D nearest(Node x, Point2D p, double distanceSquared) {
        
        Point2D nearestPoint = null;
        double nearestDistanceSquared = distanceSquared;
        
        if (x == null)                                      return null;
        if (x.rect.distanceSquaredTo(p) >= distanceSquared) return null;
                    
        double d = p.distanceSquaredTo(x.p);
        if (d < nearestDistanceSquared) {
            nearestPoint = x.p;
            nearestDistanceSquared = d;
        }
        
        // Explore both subtrees
        Node first = x.lb;
        Node second = x.rt;
        
        // First explore subtree on the same side  of the splitting line as the query point
        if (first != null && second != null) {
            if (first.rect.distanceSquaredTo(p) > second.rect.distanceSquaredTo(p)) {
                first = x.rt;
                second = x.lb;
            }
        }
        
        Point2D firstNearestPoint = nearest(first, p, nearestDistanceSquared);
        if (firstNearestPoint != null) {
            d = p.distanceSquaredTo(firstNearestPoint);
            if (d < nearestDistanceSquared) {
                nearestPoint = firstNearestPoint;
                nearestDistanceSquared = d;
            }
        }
        
        Point2D secondNearestPoint = nearest(second, p, nearestDistanceSquared);
        if (secondNearestPoint != null) {
            d = p.distanceSquaredTo(secondNearestPoint);
            if (d < nearestDistanceSquared) {
                nearestPoint = secondNearestPoint;
                nearestDistanceSquared = d;
            }
        }
        
        return nearestPoint;
    }

    /***********************************************************************
    *  Compare points
    ***********************************************************************/
    
    // compare points according to their x-coordinate
    private int xcompare(Point2D p, Point2D q) {
        if (p.x() < q.x()) return -1;
        if (p.x() > q.x()) return +1;
        return 0;
    }

    // compare points according to their y-coordinate
    private int ycompare(Point2D p, Point2D q) {
        if (p.y() < q.y()) return -1;
        if (p.y() > q.y()) return +1;
        return 0;
    }
    
    // unit testing of the methods (optional) 
    public static void main(String[] args) {
    }                
}