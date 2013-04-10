package client;

import java.io.ObjectInputStream.GetField;

import lagern.Fach;
import lagern.Lager;
import lagern.LagerHelper;
import lagern.TFachlisteHolder;
import lagern.FachPackage.EInvalidCount;
import lagern.FachPackage.ENotEnoughPieces;
import lagern.LagerPackage.EAlreadyExists;
import lagern.LagerPackage.ENotFound;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;



/**
 * 
 * @author 
 *
 */
public class Client {
	
	static Lager lager;
	
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
	    
		try {
			System.out.println(args);
			System.out.println("Arg4:" + args[4] );
		    // zugang zum namensdienst:
			ORB orb = ORB.init(args, null);
			// verbinden mit namensdienst:
			org.omg.CORBA.Object ogjRef = orb.resolve_initial_references("NameService");
			//
			NamingContextExt ncRef = NamingContextExtHelper.narrow(ogjRef);
			lager = LagerHelper.narrow(ncRef.resolve_str(args[4]));  // args[4] = lagername

			//System.out.println(lager.neu("Test"));
			
			switch(args[5]) {    // args[5] = funktion
			    case "ende":
			        quit();
		        break;
		        
			    case "liste":
			        liste();
			    break;
			    
			    case "neu":
			        neu(args[6]);    // args[6] = lagerfachname
		        break;
		        
			    case "entferne":
			        entferne(args[6]);    // args[6] = lagerfachname
			    break;
			    
			    case "einlagern":
			        einlagern(args[6], Integer.parseInt(args[7]));   //args[6] = lagerfachname, args[7] = anzahl
			    break;
			        
			    case "auslagern":
			        auslagern(args[6], Integer.parseInt(args[7]));   //args[6] = lagerfachname, args[7] = anzahl
			    break;
			    
			    case "testloop":
			        testLoop(Integer.parseInt(args[6]));     //args[6] = anzahl
			}//switch
		} catch (InvalidName e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CannotProceed e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//try
		
	}//main
	
	
	/**
	 * 
	 */
	private static void quit() {
	    lager.exit();
	}//quit
	
	
	/**
	 * 
	 */
	private static void liste() {
		TFachlisteHolder faecher = new TFachlisteHolder();
	    int anzhalFaecher = lager.getFachliste(faecher);//
	    System.out.println("Anzahl: " + anzhalFaecher);
	    for(Fach f: faecher.value)
	        System.out.printf("Fach: %s, Anzahl Teile: %d", f.name(), f.anzahl());
	}//list
	
	/**
	 * 
	 * @param name
	 */
	private static void neu(String name) {
	    try {
            lager.neu(name);
        } catch (EAlreadyExists e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }//try
	}//create
	
	
	/**
	 * 
	 * @param name
	 */
	private static void entferne(String name) {
	    try {
            lager.loeschen(name);
        } catch (ENotFound e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }//try
	}//delete
	
	
	/**
	 * 
	 * @param name
	 * @param anzahl
	 */
	private static void einlagern(String name, int anzahl) {
	    try {
            Fach fach = lager.hole(name);
            try {
                fach.einlagern(anzahl);
            } catch (EInvalidCount e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }//try
        } catch (ENotFound e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }//try
	}//store
	
	
	/**
	 * 
	 * @param name
	 * @param anzahl
	 */
	private static void auslagern(String name, int anzahl) {
	    try {
            Fach fach = lager.hole(name);
            try {
                fach.auslagern(anzahl);
            } catch (EInvalidCount e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ENotEnoughPieces e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }//try
        } catch (ENotFound e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }//try
	}//release
	
	
	/**
	 * 
	 * @param anzahl Anzahl Einlagerungen
	 */
	private static void testLoop(int anzahl) {
	    try {
            lager.neu("testFach");
        } catch (EAlreadyExists e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }//try
	    
	    Fach fach = null;
        try {
            fach = lager.hole("testFach");
        } catch (ENotFound e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }//try
        
	    for(int i = 0 ; i < anzahl ; i++)
            try {
                fach.einlagern(1);
            } catch (EInvalidCount e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }//try
	    if(fach.anzahl() == anzahl)
	        System.out.printf("%d Einlagerungen fehlerfrei vorgenommen.", anzahl);
	    else
	        System.out.printf("Fehler beim Einlagern");
	}//testLoop

}//Client
