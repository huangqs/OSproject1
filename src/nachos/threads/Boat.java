package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
	System.out.println("\n ***Testing Boats with only 2 children***");
	begin(0, 2, b);

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);
    }
    
    //////Global variables
    static int BoatLocation; // 0 for Oahu, 1 for Molokai
    static int Weight; // 50kg per child, 100kg per adult  on boat
    static int Total; // number of all people
    static int OnMolokai; // number of people get in Molokai
    static int ChOahu; // number of children on Oahu
    static Lock BoatLock = new Lock();
    static Condition WaitOahu = new Condition(BoatLock);
    static Condition WaitMolokai = new Condition(BoatLock);
    static Condition WaitFull = new Condition(BoatLock);
    //////Global variables
    
    public static void begin( int adults, int children, BoatGrader b ) 
    {
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;

	// Instantiate global variables here
	BoatLocation = 0;
	Weight = 0;
	Total = adults + children;
	OnMolokai = 0;
	ChOahu = children;
	
	
	
	// Create threads here. See section 3.4 of the Nachos for Java
	
	Runnable cr = new Runnable() {
	    public void run() {
	    		ChildItinerary(0);
            }
        };
    Runnable ar = new Runnable() {
    	    public void run() {
    	    		AdultItinerary(0);
                }
          };
     for(int i=1; i<= children;i++){
    	 KThread t = new KThread(cr);
    	 t.setName("child"+i);
    	 t.fork();
     }
     
     for(int i=1; i<= adults;i++){
    	 KThread t = new KThread(ar);
    	 t.setName("adult"+i);
    	 t.fork();
     }
     Runnable r = new Runnable() {
 	    public void run() {
 	    		while(OnMolokai != Total){
 	    			System.out.println("@.@ there are "+OnMolokai+" people on molokai already");
 	    		}
 	    		System.out.println("@.@ all people pass!");
             }
       };
       KThread t = new KThread(r);
  	 t.setName("main thread");
  	 t.fork();
	// Walkthrough linked from the projects page.
	/*
	Runnable r = new Runnable() {
	    public void run() {
                SampleItinerary();
            }
        };
        KThread t = new KThread(r);
        t.setName("Sample Boat Thread");
        t.fork();
	*/
    }

    static void AdultItinerary(int loc)
    {
	bg.initializeAdult(); //Required for autograder interface. Must be the first thing called.
	//DO NOT PUT ANYTHING ABOVE THIS LINE. 

	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
	int Location = loc;
	while(true){
		BoatLock.acquire();
		if(Location == 0){ //Oahu
			while(BoatLocation != 0 || Weight == 100 ||ChOahu>1){
				WaitOahu.sleep();
			}
			Weight += 100;
			bg.AdultRowToMolokai();
			BoatLocation = 1;
			Location = 1;
			OnMolokai += 1;
			Weight -= 100;
			WaitMolokai.wakeAll();
			WaitMolokai.sleep();
		}
		else{//Molokai
			WaitMolokai.sleep();
		}
		BoatLock.release();
		}
    }

    static void ChildItinerary(int loc)
    {
	bg.initializeChild(); //Required for autograder interface. Must be the first thing called.
	//DO NOT PUT ANYTHING ABOVE THIS LINE. 
	
	int Location = loc;
	while(true){
		BoatLock.acquire();
		if(Location == 0){//Oahu
			while(BoatLocation !=0 ||Weight==100||ChOahu==1){
				WaitOahu.sleep();
			}
			WaitOahu.wakeAll();
			if(Weight==0){// first
				Weight += 50;
				WaitFull.sleep();
				bg.ChildRideToMolokai();
				Location = 1;
			}
			else{
				Weight += 50;
				bg.ChildRowToMolokai();
				WaitFull.wakeAll();// all or no all, same
				ChOahu -= 2;
				BoatLocation = 1;
				Location = 1;
				OnMolokai += 2;
				Weight -= 100;
				WaitMolokai.sleep();
			}
		}
		else{//Molokai
			Weight += 50;
			OnMolokai -= 1;
			BoatLocation = 0;
			Location = 0;
			Weight -= 50;
			ChOahu += 1;
			WaitOahu.wakeAll();
		}
		BoatLock.release();
		}
    }

    static void SampleItinerary()
    {
	// Please note that this isn't a valid solution (you can't fit
	// all of them on the boat). Please also note that you may not
	// have a single thread calculate a solution and then just play
	// it back at the autograder -- you will be caught.
	System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
	bg.AdultRowToMolokai();
	bg.ChildRideToMolokai();
	bg.AdultRideToMolokai();
	bg.ChildRideToMolokai();
    }
    
}
