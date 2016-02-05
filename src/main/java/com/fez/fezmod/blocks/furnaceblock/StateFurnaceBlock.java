package com.fez.fezmod.blocks.furnaceblock;

import java.util.Random;

import com.fez.fezmod.FezMod;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StateFurnaceBlock extends Block implements ITileEntityProvider{

    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool ENABLED = PropertyBool.create("enabled");


    public StateFurnaceBlock() {
        super(Material.rock);
        this.setCreativeTab(CreativeTabs.tabBlock);

        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        world.setBlockState(pos, state.withProperty(FACING, getFacingFromEntity(pos, placer)), 2);
    }

    public static EnumFacing getFacingFromEntity(BlockPos clickedBlock, EntityLivingBase entity) {
        return EnumFacing.getFacingFromVector(
            (float) (entity.posX - clickedBlock.getX()),
            (float) (entity.posY - clickedBlock.getY()),
            (float) (entity.posZ - clickedBlock.getZ()));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(FACING, EnumFacing.getFront(meta & 7))
                .withProperty(ENABLED, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex() + (state.getValue(ENABLED) ? 8 : 0);
    }


    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, FACING, ENABLED);
    }
    
    
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
		// Uses the gui handler registered to your mod to open the gui for the given gui id
		// open on the server side only  (not sure why you shouldn't open client side too... vanilla doesn't, so we better not either)
		if (worldIn.isRemote) return true;

		playerIn.openGui(FezMod.instance, GuiHandlerFurnaceBlock.getGuiID(), worldIn, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityFurnaceBlock();
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (tileEntity instanceof IInventory) {
			InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory)tileEntity);
		}
		super.breakBlock(worldIn, pos, state);
	}
	
	/*
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
	{
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (tileEntity instanceof TileEntityFurnaceBlock) {
			TileEntityFurnaceBlock tileEntityFurnaceBlock = (TileEntityFurnaceBlock)tileEntity;
			boolean boolBurning = false;
			int burningSlots = tileEntityFurnaceBlock.numberOfBurningFuelSlots();
			if (burningSlots > 0) boolBurning = true;
			return getDefaultState().withProperty(ENABLED, boolBurning);//.withProperty(FACING, EnumFacing.NORTH);
		}
		return state;
	}*/
	
	@Override
	public int getLightValue(IBlockAccess world, BlockPos pos) {
		int lightValue = 0;
		IBlockState blockState = getActualState(getDefaultState(), world, pos);
		boolean boolBurning = (boolean)blockState.getValue(ENABLED);

   	if (boolBurning) {
			lightValue = 15;
		} else {
			// linearly interpolate the light value depending on how many slots are burning
			lightValue = 0;
		}
		return lightValue;
	}

	public static void setState(boolean active, World worldObj, BlockPos pos) {
        IBlockState iblockstate = worldObj.getBlockState(pos);
        TileEntity tileentity = worldObj.getTileEntity(pos);

        if (active)
        {
            worldObj.setBlockState(pos, Block.getBlockFromName("fezmod:statefurnaceblock").getDefaultState().withProperty(FACING, iblockstate.getValue(FACING)).withProperty(ENABLED, true), 3);
            worldObj.setBlockState(pos, Block.getBlockFromName("fezmod:statefurnaceblock").getDefaultState().withProperty(FACING, iblockstate.getValue(FACING)).withProperty(ENABLED, true), 3);
        }
        else
        {
            worldObj.setBlockState(pos, Blocks.furnace.getDefaultState().withProperty(FACING, iblockstate.getValue(FACING)), 3);
            worldObj.setBlockState(pos, Blocks.furnace.getDefaultState().withProperty(FACING, iblockstate.getValue(FACING)), 3);
        }

        if (tileentity != null)
        {
            tileentity.validate();
            worldObj.setTileEntity(pos, tileentity);
        }
		
	}
}
