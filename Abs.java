abstract class Abs{  
  abstract void run();  
}  
class Honda4 extends Abs{  
void run(){System.out.println("running safely");}  
public static void main(String args[]){  
 Bike obj = new Honda4();  
 obj.run();  
}  
}  