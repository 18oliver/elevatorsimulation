
public class test {/*
   int passenCount = 0;
   for(int i=0;i<boarding.size();i++) {
      passenCount += boarding.get(i).getNumberOfPassengers();
   }
   delay += Math.ceil((double)(passenCount)/(double)(el.getPassPerTick()));
//}
delay--;

if(delay>0) {
   return 4;
} else {
   return 5;
}
   if(!floors[el.getCurrFloor()].isEmpty(el.getDirection())) {
      return 4;
   } else {
      boolean directionChange = true;
      for(int i = el.getCurrFloor(); i<NUM_FLOORS; i++) {
         if(!floors[i].isEmpty(i))
            directionChange = false;
      }
      if(directionChange && el.getPassengers() == 0) {
         el.setDirection(el.getDirection()*-1);
      }
      if(!floors[el.getCurrFloor()].isEmpty(el.getDirection())) 
         return 4;
      return 5;
   }*/
   public static void main(String[] args) {
      System.out.println(Math.ceil((4.0/3.0)));
   }
}
