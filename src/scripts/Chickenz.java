package scripts;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Random;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Banking;
import org.tribot.api2007.GroundItems;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills;
import org.tribot.api2007.Skills.SKILLS;
import org.tribot.api2007.Walking;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Painting;
import org.tribot.api.util.abc.*;

@ScriptManifest(authors="snkb", category = "Combat", name = "ChickenPro")
public class Chickenz extends Script implements Painting{
	
	private final int[] chickenLoot = new int[]{526, 314};
    private final ABCUtil abc = new ABCUtil();
	private RSTile last_chicken_tile;

	private final long startTime = System.currentTimeMillis();
	private final int startHPXP = Skills.getXP(SKILLS.HITPOINTS);
	private final int startAtkXP = Skills.getXP(SKILLS.ATTACK);
	private final int startDefXP = Skills.getXP(SKILLS.DEFENCE);
	private final int startStrXP = Skills.getXP(SKILLS.STRENGTH);
	private int totalProfit = 0;
	
	//update this later by getting live price from ge api
	private final int bonesPrice = 48;
	private final int featherPrice = 3;
	
	public boolean onStartUp(){
		System.out.println("Chicken slaying god has commenced.");
		return true;
	}
	
	@Override
	public void run() {
		if(onStartUp()){
			while(true){
				sleep(2, pause());
				if(isAtChickens()){
					if(Inventory.isFull()){
						goToBank();
					}
					else{
						slayChickens();
						pickUpLoot();
					}
				}
				else if(isAtBank()){
					if(Inventory.isFull()){
						bankLoot();
					}
					else{
						goToChickens();
					}
				}
				else{
					if(Inventory.isFull()){
						goToBank();
					}
					else{
						goToChickens();
					}					
				}
			}
		}
	}
	
	private int pause(){
		Random randomNum = new Random();
		return randomNum.nextInt(25);
	}
	
	private boolean isAtChickens(){
		//System.out.println("At kfc");
		final RSNPC[] chickens = NPCs.findNearest("Chicken");
		if(chickens.length <= 0){
			return false;
		}
		return chickens[0].isOnScreen();
	}
	
	private boolean isAtBank(){
		//System.out.println("At bank");
		final RSObject[] bankBooths = Objects.findNearest(30, "Bank Booth");
		if(bankBooths.length > 1){
			if(bankBooths[0].isOnScreen()){
				return true;
			}
		}
		final RSNPC[] bankers = NPCs.findNearest("Banker");
		if(bankers.length <= 0){
			return false;
		}
		return bankers[0].isOnScreen();
	}
	
	private boolean bankLoot(){
		System.out.println("EZ MONEY");
		if(!Banking.isBankScreenOpen()){
			if(!Banking.openBank()){
				return false;
			}
		}
		
		int currFeatherCount = Inventory.getCount(314);
		int currBonesCount = Inventory.getCount(526);
		totalProfit += ((currFeatherCount * featherPrice) + (currBonesCount * bonesPrice));
		if(Banking.depositAll() < 1){
			return false;
		}
		if(Banking.isBankScreenOpen()){
			Banking.close();
		}
		
		return Timing.waitCondition(new Condition(){
			@Override
			public boolean active(){
				return !Inventory.isFull();
			}
		}, General.random(4000, 5100));
	}
	
	private boolean goToChickens(){
		System.out.println("I could do for some chicken right now.");
		while(Player.getPosition().getPlane() != 0){
			if(!Player.isMoving()){
				if(Player.getPosition().getPlane() == 2){
					Random rand = new Random();
					RSTile outsideBank = new RSTile(3200 + rand.nextInt(8), 3202 + rand.nextInt(8));
					WebWalking.walkTo(outsideBank);
				}
				System.out.println(Player.getPosition().getPlane());
				RSObject[] stairs = Objects.findNearest(40, "Staircase");
				if(stairs.length <= 0){
					System.out.println("No stairs to be found.");
					break;
				}
				else{
					stairs[0].click("Climb-down");
					General.sleep(270, 460);
				}
			}
		}
		Random rand = new Random();
		int randTile1 = rand.nextInt(9);
		int randTile2 = rand.nextInt(11);
	    RSTile OUTSIDE_GATE_TILE = new RSTile(3230+randTile1, 3287 + randTile2);

		if(!WebWalking.walkTo(OUTSIDE_GATE_TILE)){
			System.out.println("Going to chicken coop");
			return false;
		}
		
		final RSNPC[] chickens = NPCs.findNearest("Chicken");
		
		if(chickens.length <= 0){
			System.out.println("No chickens to be found.");
			return false;
		}

		RSTile chickenTile = chickens[0].getPosition().translate(General.random(-1, 1), General.random(-1, 1));
		WebWalking.walkTo(chickenTile);
	
		return Timing.waitCondition(new Condition(){
			@Override 
			public boolean active(){
				General.sleep(250, 530);
				return isAtChickens();
			}
		}, General.random(7600, 9100));
	}
	
	private boolean goToBank(){
		System.out.println("going to bank the loots.");
		if(!WebWalking.walkToBank()){
			return false;
		}
		return Timing.waitCondition(new Condition(){
			@Override
			public boolean active(){
				General.sleep(270, 420);
				return isAtBank();
			}
		}, General.random(9300, 10490));
	}
	
	private boolean slayChickens(){
		if(inCombat()){
			final long timeout = System.currentTimeMillis() + General.random(40000, 70000); 
			while(inCombat() && System.currentTimeMillis() < timeout){
				sleep(80, 170);
				if(this.last_chicken_tile != null){
					if(!NPCs.isAt(this.last_chicken_tile, "Chicken")){
						break;
					}
				}
			}
		}
		
		final RSNPC[] chickens = NPCs.findNearest("Chicken");
		if(chickens.length <= 0){
			return false;
		}
		if(!chickens[0].isOnScreen()){
			if(!Walking.walkPath(Walking.generateStraightPath(chickens[0]))){
				return false;
			}
			
			if(!Timing.waitCondition(new Condition(){
				@Override
				public boolean active(){
					General.sleep(110);
					return chickens[0].isOnScreen();
				}
			}, General.random(7800, 8785)));
			return false;
		}
		if(!chickens[0].isInCombat() && !chickens[0].isInteractingWithMe() && chickens[0].isValid()){
			System.out.println("On the attack!");
			DynamicClicking.clickRSNPC(chickens[0], "Attack Chicken");
			//return inCombat();
		}
		
		if(abc.shouldCheckXP()){
			abc.checkXP();
		}
		
		if(Timing.waitCondition(new Condition(){
			@Override
			public boolean active(){
				return inCombat();
			}
		}, General.random(5000, 6200))){
			this.last_chicken_tile = chickens[0].getPosition().clone();
			return true;
		};
		return false;
	}
	
	private void pickUpLoot(){
		System.out.println("picking up loot.");
        RSGroundItem[] chickenDrops = GroundItems.findNearest(chickenLoot);
        Random rand = new Random();
        if(chickenDrops.length > 0){
        	double missclick = rand.nextDouble();
        	if(missclick <= .133){
        		System.out.println("Missclicked");
        		RSTile featherTile = chickenDrops[0].getPosition();
        		int newX = featherTile.getX() + rand.nextInt(4);
        		int newY = featherTile.getY() - rand.nextInt(3);
        		DynamicClicking.clickRSTile(new RSTile(newX, newY), "Oops");
        	}
        	else{
	        	System.out.println("Gimme gimme.");
	        	if(chickenDrops[0].getID() == 526){
	        		DynamicClicking.clickRSGroundItem(chickenDrops[0], "Take Bones");
	        	}
	        	else{
	        		DynamicClicking.clickRSGroundItem(chickenDrops[0], "Take Feather");
	        	}
        	}
        }
        
        if(abc.shouldCheckTabs()){
        	abc.checkTabs();
        }
       
        General.sleep(280, 440);
	    //chickenDrops = GroundItems.findNearest(chickenLoot);
	}
	
	private boolean inCombat(){
		//System.out.println("fighting chicken.");
		return Player.getAnimation() > 0;
	}
	
	@Override
	public void onPaint(Graphics g){
		long runTime = System.currentTimeMillis() - startTime;
		double elapsedTimeInSec = runTime/1000.0;
		g.setColor(Color.CYAN);
		g.drawString("Pro Chickenz - 1.0", 10, 260);
		g.drawString("Runtime: " + Timing.msToString(runTime), 10, 280);
		int currFeatherCount = Inventory.getCount(314);
		int currBonesCount = Inventory.getCount(526);
		int currentProfit = ((currFeatherCount * featherPrice) + (currBonesCount * bonesPrice));
		if(currentProfit < 0){
			currentProfit = 0;
		}
		
		double profitPerHour = 0;
		if(elapsedTimeInSec > 0){
			profitPerHour = Math.round((((totalProfit+currentProfit)/elapsedTimeInSec)*3600)*100.0)/100.0;
		}
		
		g.drawString("Profit per hour: " + profitPerHour + "", 10, 300);
		int currHPXP = Skills.getXP(SKILLS.HITPOINTS);
		int currAtkXP = Skills.getXP(SKILLS.ATTACK);
		int currDefXP = Skills.getXP(SKILLS.DEFENCE);
		int currStrXP = Skills.getXP(SKILLS.STRENGTH);
		int totalXPEarned = ((currHPXP + currAtkXP + currDefXP + currStrXP) - 
								(startHPXP + startAtkXP + startDefXP + startStrXP));
		double xpPerHour = 0;
		if(elapsedTimeInSec > 0){
			xpPerHour = Math.round(((totalXPEarned/elapsedTimeInSec)*3600)*100.0)/100.0;
		}
		//System.out.println("exp earned: " + totalXPEarned + " / elapsed time: " + elapsedTimeInSec);
		g.drawString("EXP per hour: " + xpPerHour, 10, 320);
	}
	
}
