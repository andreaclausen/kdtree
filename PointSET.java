public class PointSET {
    
    private SET<Point2D> set = new SET<Point2D>();
    
    // construct an empty set of points 
    public PointSET() {
    }                               
    
    // is the set empty?
    public boolean isEmpty() {
        return set.isEmpty();
    }                      
    
    // number of points in the set
    public int size() {
        return set.size();
    }                      
    
    // add the point to the set (if it is not already in the set)
    public void insert(Point2D p) {
        set.add(p);
    }            
    
    // does the set contain point p?
    public boolean contains(Point2D p) {
        return set.contains(p);
    }           
    
    // draw all points to standard draw
    public void draw() {
    }                        
    
    // all points that are inside the rectangle 
    public Iterable<Point2D> range(RectHV rect) {
        
        Stack<Point2D> s = new Stack<Point2D>();
        
        // traverse the BST
        for (Point2D p : set) {
            // if point in rectangle, add to queue
            if (rect.contains(p)) s.push(p);
        }
        
        return s;
    }            
    
    // a nearest neighbor in the set to point p; null if the set is empty
    public Point2D nearest(Point2D p) {
        
        Point2D nearest = null;
        
        // traverse the BST
        for (Point2D p1 : set)
            // update nearest neighbor
            if (nearest == null)                               nearest = p1;
            else if (p1.distanceTo(p) < nearest.distanceTo(p)) nearest = p1;
        
        return nearest;
    }           

    // unit testing of the methods (optional)
    public static void main(String[] args) {
    }              
}