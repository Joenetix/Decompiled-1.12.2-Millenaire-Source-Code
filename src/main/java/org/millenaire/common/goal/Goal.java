package org.millenaire.common.goal;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.annotedparameters.AnnotedParameter;
import org.millenaire.common.annotedparameters.ConfigAnnotations;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.pathing.atomicstryker.AStarConfig;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.Building;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for all villager goals.
 * Ported from 1.12.2 to 1.20.1 with MillVillager as primary entity type.
 */
public abstract class Goal {

    public static final int STANDARD_DELAY = 2000;
    public static Map<String, Goal> goals = new HashMap<>();

    // Static goal references
    public static GoalSleep sleep;
    public static GoalBeSeller beSeller;
    public static Goal construction;
    public static Goal deliverGoodsHousehold;
    public static Goal getResourcesForBuild;
    public static Goal raidVillage;
    public static Goal defendVillage;
    public static Goal hide;
    public static Goal gettool;
    public static Goal gosocialise;

    // Pathing configurations
    public static final AStarConfig JPS_CONFIG_TIGHT = new AStarConfig(true, false, false, false, true);
    public static final AStarConfig JPS_CONFIG_WIDE = new AStarConfig(true, false, false, false, true, 2, 10);
    public static final AStarConfig JPS_CONFIG_BUILDING = new AStarConfig(true, false, false, false, true, 2, 60);
    public static final AStarConfig JPS_CONFIG_BUILDING_SCAFFOLDINGS = new AStarConfig(true, false, false, false, true,
            2, 5);
    public static final AStarConfig JPS_CONFIG_CHOPLUMBER = new AStarConfig(true, false, false, false, true, 4, 60);

    // Tag constants
    public static final String TAG_CONSTRUCTION = "tag_construction";
    public static final String TAG_AGRICULTURE = "tag_agriculture";
    protected static final int ACTIVATION_RANGE = 3;

    // Instance fields
    public String key;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, defaultValue = "false")
    @ConfigAnnotations.FieldDocumentation(explanation = "If true, this is a leisure activity that can be interrupted by other goals.")
    public boolean leasure = false;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, defaultValue = "true")
    @ConfigAnnotations.FieldDocumentation(explanation = "If true, villagers performing this goal will move faster than normal.")
    public boolean sprint = true;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "tag")
    @ConfigAnnotations.FieldDocumentation(explanation = "A tag to use when referring to the goal elsewhere.")
    public List<String> tags = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM_NUMBER_ADD)
    @ConfigAnnotations.FieldDocumentation(explanation = "If more than that number of item is present in the building, stop goal.")
    public HashMap<InvItem, Integer> buildingLimit = new HashMap<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM_NUMBER_ADD)
    @ConfigAnnotations.FieldDocumentation(explanation = "If more than that number of item is present in the town hall, stop goal.")
    public HashMap<InvItem, Integer> townhallLimit = new HashMap<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM_NUMBER_ADD)
    @ConfigAnnotations.FieldDocumentation(explanation = "If more than that number of item is present in the village, stop goal.")
    public HashMap<InvItem, Integer> villageLimit = new HashMap<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "0")
    @ConfigAnnotations.FieldDocumentation(explanation = "No more than X villagers doing this goal in the same building at the same time.")
    public int maxSimultaneousInBuilding = 0;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "0")
    @ConfigAnnotations.FieldDocumentation(explanation = "No more than X villagers doing this goal at the same time.")
    public int maxSimultaneousTotal = 0;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER)
    @ConfigAnnotations.FieldDocumentation(explanation = "Time of the day before which the goal can't be taken, in ticks.")
    public int minimumHour = -1;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER)
    @ConfigAnnotations.FieldDocumentation(explanation = "Time of the day after which the goal can't be taken, in ticks.")
    public int maximumHour = -1;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN)
    @ConfigAnnotations.FieldDocumentation(explanation = "Whether this goal should get displayed in a villager's travel book sheet.")
    public boolean travelBookShow = true;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM)
    @ConfigAnnotations.FieldDocumentation(explanation = "Name of a good whose icon represents this goal.")
    protected InvItem icon = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM)
    @ConfigAnnotations.FieldDocumentation(explanation = "Name of a good whose icon will float above the villager's head when performing this goal.")
    protected InvItem floatingIcon = null;

    public Goal() {
        tags = new ArrayList<>();
    }

    // --- Static Initialization ---

    public static void initGoals() {
        goals = new HashMap<>();

        // Register leisure goals
        goals.put("gorest", new GoalGoRest());
        gosocialise = new GoalGoSocialise();
        goals.put("gosocialise", gosocialise);
        goals.put("chat", new GoalGoChat());

        // Register core goals
        goals.put("gathergoods", new GoalGatherGoods());
        goals.put("bringbackresourceshome", new GoalBringBackResourcesHome());
        gettool = new GoalGetTool();
        goals.put("getitemtokeep", gettool);
        goals.put("huntmonster", new GoalHuntMonster());
        goals.put("getgoodshousehold", new GoalGetGoodsForHousehold());

        sleep = new GoalSleep();
        goals.put("sleep", sleep);

        deliverGoodsHousehold = new GoalDeliverGoodsHousehold();
        goals.put("delivergoodshousehold", deliverGoodsHousehold);
        goals.put("gethousethresources", new GoalGetResourcesForShops());
        goals.put("deliverresourcesshop", new GoalDeliverResourcesShop());

        goals.put("choptrees", new GoalLumbermanChopTrees());
        goals.put("plantsaplings", new GoalLumbermanPlantSaplings());

        getResourcesForBuild = new GoalGetResourcesForBuild();
        goals.put("getresourcesforbuild", getResourcesForBuild);

        beSeller = new GoalBeSeller();
        goals.put("beseller", beSeller);

        construction = new GoalConstructionStepByStep();
        goals.put("construction", construction);
        goals.put("buildpath", new GoalBuildPath());
        goals.put("clearoldpath", new GoalClearOldPath());

        raidVillage = new GoalRaidVillage();
        goals.put("raidvillage", raidVillage);

        defendVillage = new GoalDefendVillage();
        goals.put("defendvillage", defendVillage);

        hide = new GoalHide();
        goals.put("hide", hide);

        goals.put("becomeadult", new GoalChildBecomeAdult());
        goals.put("shearsheep", new GoalShearSheep());
        goals.put("breed", new GoalBreedAnimals());
        goals.put("mining", new GoalMinerMineResource());
        goals.put("visitinn", new GoalMerchantVisitInn());
        goals.put("visitbuilding", new GoalMerchantVisitBuilding());
        goals.put("keepstall", new GoalForeignMerchantKeepStall());

        goals.put("drybrick", new GoalIndianDryBrick());
        goals.put("gatherbrick", new GoalIndianGatherBrick());
        goals.put("plantsugarcane", new GoalIndianPlantSugarCane());
        goals.put("harvestsugarcane", new GoalIndianHarvestSugarCane());

        goals.put("performpujas", new GoalPerformPuja());
        goals.put("bepujaperformer", new GoalBePujaPerformer());

        goals.put("fish", new GoalFish());
        goals.put("fishinuit", new GoalFishInuit());
        goals.put("harvestwarts", new GoalHarvestWarts());
        goals.put("plantwarts", new GoalPlantNetherWarts());
        goals.put("brewpotions", new GoalBrewPotions());

        goals.put("gathersilk", new GoalByzantineGatherSilk());
        goals.put("gathersnails", new GoalByzantineGatherSnails());
        goals.put("plantcocoa", new GoalPlantCacao());
        goals.put("harvestcocoa", new GoalHarvestCacao());

        // Load generic goals
        org.millenaire.common.goal.generic.GoalGeneric.loadGenericGoals();

        // Set keys and add self-tag
        for (String s : goals.keySet()) {
            goals.get(s).key = s;
            goals.get(s).tags.add(s);
        }
    }

    // --- MillVillager-based Methods (Primary API) ---

    public int actionDuration(MillVillager villager) throws Exception {
        return 10;
    }

    public boolean allowRandomMoves() throws Exception {
        return false;
    }

    public boolean autoInterruptIfNoTarget() {
        return true;
    }

    public boolean canBeDoneAtNight() {
        return false;
    }

    public boolean canBeDoneInDayTime() {
        return true;
    }

    public GoalInformation getDestination(MillVillager villager) throws Exception {
        return null; // Subclasses override
    }

    public ItemStack getFloatingIcon() {
        return floatingIcon == null ? ItemStack.EMPTY : floatingIcon.getItemStack();
    }

    public ItemStack[] getHeldItemsDestination(MillVillager villager) throws Exception {
        return getHeldItemsTravelling(villager);
    }

    public ItemStack[] getHeldItemsOffHandDestination(MillVillager villager) throws Exception {
        return getHeldItemsOffHandTravelling(villager);
    }

    public ItemStack[] getHeldItemsOffHandTravelling(MillVillager villager) throws Exception {
        return null;
    }

    public ItemStack[] getHeldItemsTravelling(MillVillager villager) throws Exception {
        return null;
    }

    public ItemStack getIcon() {
        return icon == null ? ItemStack.EMPTY : icon.getItemStack();
    }

    public AStarConfig getPathingConfig(MillVillager villager) {
        return JPS_CONFIG_TIGHT;
    }

    public boolean isFightingGoal() {
        return false;
    }

    public boolean isPossible(MillVillager villager) {
        try {
            // Check time restrictions using villager's level
            if (villager.level() != null) {
                long dayTime = villager.level().getDayTime() % 24000L;
                if (minimumHour >= 0 && dayTime < minimumHour) {
                    return false;
                }
                if (maximumHour >= 0 && dayTime > maximumHour) {
                    return false;
                }
            }
            return isPossibleSpecific(villager);
        } catch (Exception e) {
            MillLog.printException("Exception in isPossible() for goal: " + key, e);
            return false;
        }
    }

    protected boolean isPossibleSpecific(MillVillager villager) throws Exception {
        return true;
    }

    public String labelKey(MillVillager villager) {
        return key;
    }

    public String labelKeyWhileTravelling(MillVillager villager) {
        return key;
    }

    public boolean lookAtGoal() {
        return false;
    }

    public boolean lookAtPlayer() {
        return false;
    }

    public String nextGoal(MillVillager villager) throws Exception {
        return null;
    }

    public void onAccept(MillVillager villager) throws Exception {
    }

    public void onComplete(MillVillager villager) throws Exception {
    }

    protected GoalInformation packDest(Point p) {
        return new GoalInformation(p, null, null);
    }

    protected GoalInformation packDest(Point p, Building b) {
        return new GoalInformation(p, b != null ? new Point(b.getPos()) : null, null);
    }

    protected GoalInformation packDest(Point p, Building b, Entity ent) {
        return new GoalInformation(p, b != null ? new Point(b.getPos()) : null, ent);
    }

    protected GoalInformation packDest(Point p, Point p2) {
        return new GoalInformation(p, p2, null);
    }

    public boolean performAction(MillVillager villager) throws Exception {
        return true; // Default implementation
    }

    public int priority(MillVillager villager) throws Exception {
        return 50; // Default priority
    }

    public int range(MillVillager villager) {
        return 3;
    }

    public String sentenceKey() {
        return key;
    }

    public void setVillagerDest(MillVillager villager) throws Exception {
        GoalInformation dest = getDestination(villager);
        if (dest != null) {
            villager.setGoalInformation(dest);
        }
    }

    public boolean shouldVillagerLieDown() {
        return false;
    }

    public boolean stopMovingWhileWorking() {
        return true;
    }

    public boolean stuckAction(MillVillager villager) throws Exception {
        return false;
    }

    public long stuckDelay(MillVillager villager) {
        return 200L;
    }

    public boolean swingArms() {
        return false;
    }

    public boolean swingArmsWhileTravelling() {
        return false;
    }

    public boolean validateDest(MillVillager villager, Building dest) throws MillLog.MillenaireException {
        if (dest == null) {
            throw new MillLog.MillenaireException("Given null dest in validateDest for goal: " + key);
        }
        // Simplified validation
        for (InvItem item : buildingLimit.keySet()) {
            if (dest.nbGoodAvailable(item, false, false, false) > buildingLimit.get(item)) {
                return false;
            }
        }
        return true;
    }

    public AStarConfig getPathingConfig() {
        return JPS_CONFIG_TIGHT;
    }

    public String getLabel() {
        return key;
    }

    @Override
    public String toString() {
        return "goal:" + key;
    }

    // --- Inner Classes ---

    public static class GoalInformation {
        private Point dest;
        private Point destBuildingPos;
        private Entity targetEnt;

        public GoalInformation(Point dest, Point buildingPos, Entity targetEnt) {
            this.dest = dest;
            this.destBuildingPos = buildingPos;
            this.targetEnt = targetEnt;
        }

        public Point getDest() {
            return dest;
        }

        public Point getDestBuildingPos() {
            return destBuildingPos;
        }

        public Entity getTargetEnt() {
            return targetEnt;
        }

        public void setDest(Point dest) {
            this.dest = dest;
        }

        public void setDestBuildingPos(Point destBuildingPos) {
            this.destBuildingPos = destBuildingPos;
        }

        public void setTargetEnt(Entity targetEnt) {
            this.targetEnt = targetEnt;
        }
    }
}
