package scripts;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Random;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.DynamicMouse;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.util.abc.ABCUtil;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Constants.IDs.Spells;
import org.tribot.api2007.GrandExchange;
import org.tribot.api2007.GrandExchange.COLLECT_METHOD;
import org.tribot.api2007.GroundItems;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Magic;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Player;
import org.tribot.api2007.Projection;
import org.tribot.api2007.Skills;
import org.tribot.api2007.Skills.SKILLS;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Painting;

@ScriptManifest(authors="snkb", category = "Money making", name = "ZammyWinePro")
public class ZammyWinez extends Script implements Painting {

	private final int templeTileX = 2933;
	private final int templeTileY = 3515;
	private final RSTile wineTile = new RSTile(2930, 3515);
	private final int zammyWine = 245;
	private final int zammyWinePrice = 1890;
	private final int lawRunePrice = 200;
	private final long startTime = System.currentTimeMillis();
	private final ABCUtil abc = new ABCUtil();
	private final int startingMagicXP = Skills.getXP(SKILLS.MAGIC);
	private final int startingHPXP = Skills.getXP(SKILLS.HITPOINTS);
	
	private String currentAction = null;
	private int numWinesBefore = 0;
	private int totalWinesCollected = 0;
	private int totalWinesLost = 0;
	private boolean loop = true;

	public boolean onStartUp(){
		totalWinesCollected -= Inventory.getCount(zammyWine);
		System.out.println("ZammyWinez Pro - V1.0 has started.");
		return true;
	}
	
	@Override
	public void run() {
		if(onStartUp()){
			while(loop){
				sleep(pause());
				if(abc.shouldCheckXP() && new Random().nextDouble() <= .07){
					abc.checkXP();
				}
				if(Inventory.getCount(563) <= 0){
					getMoreLaws();
				}
				else{
					if(isAtZammyTemple()){
						if(Inventory.isFull()){
							currentAction = "Banking";
							goToBank();
						}
						else{
							currentAction = "Collecting Wines";
							collectWines();
						}
					}
					else if(isAtBank()){
						if(Inventory.isFull()){
							currentAction = "Depositing Wines";
							bankLoot();
						}
						else{
							currentAction = "Travelling to Temple";
							goToZammyTemple();
						}
					}
					else{
						if(Inventory.isFull()){
							currentAction = "Banking";						
							goToBank();
						}
						else{
							currentAction = "Travelling to Temple";
							goToZammyTemple();
						}
					}
				}
			}
		}
		
	}

	private boolean goToZammyTemple() {
		Random rand = new Random();
		int randX = rand.nextInt(4);
		RSTile zammyTempleTile = new RSTile(templeTileX + randX, templeTileY);
		if(!WebWalking.walkTo(zammyTempleTile)){
			System.out.println("Going to zammy temple");
			return false;
		}
	
		WebWalking.walkTo(new RSTile(2931, 3515));
	
		return Timing.waitCondition(new Condition(){
			@Override 
			public boolean active(){
				General.sleep(250, 530);
				return isAtZammyTemple();
			}
		}, General.random(7600, 9100));
	}
	
	private boolean isAtZammyTemple() {
		if(Player.getPosition().distanceTo(wineTile) <= 1){
			return true;
		}
		if(abc.shouldExamineEntity() && new Random().nextDouble() <= .1){
			abc.examineEntity();
		}
		return false;
	}

	private void collectWines() {
		RSGroundItem currentWine = getWine();
		if(currentWine != null){
			if(selectTeleGrab() && currentWine.click("Cast")){
				if(Inventory.getCount(zammyWine) > numWinesBefore){
					numWinesBefore += 1;
					totalWinesCollected += 1;
					System.out.println("Success!");
				}
				else{
					System.out.println("Unlucky");
					totalWinesLost += 1;
				}
				General.sleep(2300, 4700);
			}
		}
		else{
			if(selectTeleGrab() && !Projection.getTileBoundsPoly(wineTile, 100).contains(Mouse.getPos())){
                wineTile.hover();
            }			
		}
		General.random(3800, 6800);
	}
	
    private boolean selectTeleGrab(){
		if(Inventory.getCount(563) <= 0){
            System.out.println("Not enough runes! Stopping now!");
            loop = false;
            return false;
        }
        if (Magic.isSpellSelected()) {
            //check if the selected magic spell is telegrab.
            if (Magic.getSelectedSpellName().equals("Telekinetic Grab")) {
                return true;
            }
            //wrong spell is selected
            else {
                //deselect wrong spell.
                Mouse.click(1);
                //select the telegrab.
				if(Magic.selectSpell("Telekinetic Grab")){
                    return true;
                }
            }
        }
        //there is no spell selected yet
        else {
            if (Magic.selectSpell("Telekinetic Grab")) {
                return true;
            }
        }
        //failed to select a spell
        return false;
    }

	private RSGroundItem getWine(){
		RSGroundItem[] wines = GroundItems.findNearest(zammyWine);
		if(wines.length > 0 && wines[0] != null){
			return wines[0];
		}
		return null;
	}
	
	private boolean goToBank(){
		if(Inventory.getCount(861) > 0 && Inventory.getCount(563) > 0){
			Magic.selectSpell("Falador Teleport");
		}
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
		if(new Random().nextDouble() <= .05){
			General.random(13000, 20000);
		}
		return bankers[0].isOnScreen();
	}
	
	private boolean bankLoot(){
		System.out.println("EZ MONEY");
		numWinesBefore = 0;
		//totalProfit += ((currCowhideCount * cowhidePrice) + (currBonesCount * bonesPrice));
		if(new Random().nextDouble() >= .2){
			depositToBankBooth();
		}
		else{
			RSObject[] depositBoxes = Objects.findNearest(30, 6948);
			if(depositBoxes.length > 0){
				DynamicClicking.clickRSObject(depositBoxes[0], "Deposit Bank Deposit Box");
				if(Banking.depositAllExcept(558) < 1){
					return false;
				}
			}
			else{
				depositToBankBooth();
			}
		}
		if(Inventory.getCount(563) < 26){
			getMoreLaws();
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
	
	private boolean depositToBankBooth(){
		if(!Banking.isBankScreenOpen()){
			if(!Banking.openBank()){
				return false;
			}
		}
		if(Banking.depositAllExcept(563) < 1){
			return false;
		}
		return true;
	}
	
	private void getMoreLaws(){
		currentAction = "Getting More Laws";
		if(!isAtBank()){
			goToBank();
		}
		if(!Banking.isBankScreenOpen()){
			Banking.openBank();
		}
		if(Banking.find(563).length > 0){
			int numLawsInBank = Banking.find(563)[0].getStack();
			System.out.println("Num laws: " + numLawsInBank);
			if(numLawsInBank > 1){
				Banking.withdraw(numLawsInBank-1, 563);
				General.random(6400, 8930);
			}
		}
		else{
			/*
			if(Banking.find(554).length > 0 && Banking.find(995).length > 0){
				if(Banking.find(554)[0].getStack() > 0 && Banking.find(995)[0].getStack() > 30000){
					Banking.withdraw(1, 554);
					General.random(3560, 4800);
					Banking.withdraw(30000, 995);
					General.random(3344, 4580);
					if(Inventory.getCount(861) <= 0 && Banking.find(861)[0].getStack() > 0){
						Banking.withdraw(1, 861);
					}
					buyLawsFromGE();
				}
			}
			else{
				loop = false;
			}
			*/
			loop = false;
		}
	}
	
	private void buyLawsFromGE(){
		Magic.selectSpell("Varrock Teleport");
		int geX = new Random().nextInt(14) + 3166;
		int geY = new Random().nextInt(15) + 3420;
		WebWalking.walkTo(new RSTile(geX, geY));
		GrandExchange.goToSelectionWindow();
		GrandExchange.offer("Law rune", 250, 100, false);
		GrandExchange.collectItems(COLLECT_METHOD.ITEMS, new RSItem(563, 0, 0, null));
		GrandExchange.close();
		if(Inventory.getCount(861) > 0){
			Magic.selectSpell("Falador Teleport");
		}
	}
	
	private int pause(){
		Random randomNum = new Random();
		return randomNum.nextInt(15);
	}
	
	@Override
	public void onPaint(Graphics g) {
		long runTime = System.currentTimeMillis() - startTime;
		long elapsedTimeInSec = runTime/1000;
		g.setColor(Color.CYAN);
		g.drawString("Pro Winez - 1.0", 10, 245);
		g.drawString("Runtime: " + Timing.msToString(runTime), 10, 260);
		g.drawString("Current Status: " + currentAction, 10, 275);
		int totalLost = (totalWinesCollected * lawRunePrice) + totalWinesLost * lawRunePrice;
		int totalProfit = (totalWinesCollected * zammyWinePrice) - totalLost;
		g.drawString("Total profit: " + totalProfit, 10, 290);
		if(elapsedTimeInSec > 0){
			g.drawString("Profit per hour: " + (totalProfit/elapsedTimeInSec)*3600, 10, 305);
		}
		else{
			g.drawString("Profit per hour: 0", 10, 305);			
		}
		
		double totalWinesAttempted = 1.0;
		if(totalWinesLost+totalWinesCollected > 0){
			totalWinesAttempted = totalWinesLost+totalWinesCollected*1.0;		
		}
		double successRate = (totalWinesCollected/totalWinesAttempted)*100;
		g.drawString("Wine grab success rate: " + successRate + "%", 10, 320);
		
		int totalXPEarned = (Skills.getXP(SKILLS.MAGIC) + Skills.getXP(SKILLS.HITPOINTS)) - (startingHPXP + startingMagicXP);
		if(elapsedTimeInSec > 0){
			g.drawString("EXP per hour: " + (totalXPEarned/elapsedTimeInSec)*3600, 10, 335);
		}
	}

}
