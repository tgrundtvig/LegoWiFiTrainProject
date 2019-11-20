

//Set this to 0 for the small footprint or 1 for the big footprint
node_type = 0;
//Set this to one, to create a bottom for arduino pro mini.
arduino_bottom = 0;




//BasePlate();
//MotorCut();
//Bottom();
//BottomWithMagnet();
//BottomWithSensorLow();
//StepperAxis();
//Assambled();
//translate([40,80,0])
//NodeMCUV3Holder();
//NodeMCUV2Holder();
//ULN2003Holder();
//translate([5,80,0])
//rotate([0,0,60])
//MotorTop();
//translate([-40,25,0])
//rotate([0,0,90])
//Switcher();
//ConnectionInterface();
//BlueSwitchWiFiPrint();
//TwoLeftSwitches();
//FourSwitches();
//StepperAxis();
//plateOfMagnetBricks(4,10);
//magnetBrick();
//roundMagnetBrick();
//hallSensorBrick();
//makeStandardLegoBrick(4,2,3);
//magnetTest();
//hallSensorBrickBoogie();
//hallSensorBrickBoogieDouble();
//hallSensorBrickBoogieWithMagnet();
//hallSensorBrickLow();

//translate([0,0,3])
//makeStandardLegoBrickBottom(2,2,1);
//makeStandardLegoBrick(2,2,1);
//makeStuds(8,2);


//Settings
print_width = 0.4; 
stud_diameter = 4.84;
stud_height = 1.9;
base_height = 3.0;
base_width = 8;
stud_offset = 0.5 * base_width;
edge_offset = 0.2;
edge_width = 0.5*(base_width-stud_diameter-0.2);
ring_diameter_outer = 6.2;
ring_diameter_inner = stud_diameter+0.2;
z_offset = 0.2;

motor_height = arduino_bottom ? 22 : 15;
switcher_height = motor_height - 6;

//Magnets for registrering train travel
magnet_height = 8;
magnet_x = 10.2;
magnet_y = 5.2;
magnet_z = 3.4;

module FourSwitches()
{
    if(arduino_bottom)
    {
        translate([-90,0,0])
        mirror()
        TwoLeftSwitches();
        translate([90,0,0])
        TwoLeftSwitches();
    }
    else
    {
        translate([-65,0,0])
        mirror()
        TwoLeftSwitches();
        translate([65,0,0])
        TwoLeftSwitches();
    }
}

module TwoLeftSwitches()
{
    if(arduino_bottom)
    {
        translate([-21,-72,0])
        BlueSwitchWiFiPrint();
        translate([21,72,0])
        rotate([0,0,180])
        BlueSwitchWiFiPrint();
    }
    else
    {
        translate([-0,-111,0])
        BlueSwitchWiFiPrint();
        translate([0,111,0])
        rotate([0,0,180])
        BlueSwitchWiFiPrint();
    }
}

module BlueSwitchWiFiPrint()
{
    BottomWithSensorLow();
    translate([-46.5,27,0])
    SwitcherLoose();
    if(!arduino_bottom)
    {
        if(node_type == 0)
            translate([38,82,0])
                NodeMCUV2Holder();
        if(node_type == 1)
            translate([38,82,0])
                NodeMCUV3Holder();
        translate([-2,85.2,0])
        rotate([0,0,45+180])
        MotorTop();
    }
}

module Assambled()
{   
    Bottom();
    translate([-20,48,base_height])
    rotate([0,0,-45])
    union()
    {
        translate([0,0,1])
        rotate([0,0,120])
        Switcher();
        if(!arduino_bottom)
        translate([0,-8,motor_height-base_height+2+19])
        rotate([180,0,0])
        MotorTop();
    }
    
    if(node_type == 0 && !arduino_bottom)
    translate([-40.9,47.8,motor_height+21])
    rotate([0,0,180])
    NodeMCUV2Holder();
    if(node_type == 1 && !arduino_bottom)
    translate([-38.9,47.8,motor_height+21])
    rotate([0,0,180])
    NodeMCUV3Holder();
    
    
    //translate([-base_width*7-1,0,0])
    //cube([1,100,50]);
    //translate([-base_width*8,base_width*2-1,0])
    //cube([100,1,50]);
}

module NodeMCUV2Holder()
{
    translate([1,0,0])
    rotate([0,0,90])
    ConnectionInterface();
    translate([-10.5, -21.75, 0])
        NodeLeg();
    translate([10.5, -21.75, 0])
        NodeLeg();
    translate([-10.5, 21.75, 0])
        NodeLeg();
    translate([10.5, 21.75, 0])
        NodeLeg();
    translate([0,20.5,0.5*base_height])
        cube([25, 5, base_height], center=true);
    //translate([-4,21.75,0.5*base_height])
    //    cube([12, 5, base_height], center=true);
    //translate([-7.5,-26,0.5*base_height])
    //    cube([12, 5, base_height], center=true);
    translate([0,-20.5,0.5*base_height])
        cube([25, 5, base_height], center=true);
    translate([10.5,0,0.5*base_height])
        cube([5, 42, base_height], center=true);
    translate([-10.5,0,0.5*base_height])
        cube([5, 42, base_height], center=true);
}

module NodeMCUV3Holder()
{
    translate([3,0,0])
    rotate([0,0,90])
    ConnectionInterface();
    translate([-12.5, -26, 0])
        NodeLeg();
    translate([12.5, -26, 0])
        NodeLeg();
    translate([-12.5, 26, 0])
        NodeLeg();
    translate([12.5, 26, 0])
        NodeLeg();
    translate([10,26,0.5*base_height])
        cube([10, 5, base_height], center=true);
    translate([-7.5,26,0.5*base_height])
        cube([12, 5, base_height], center=true);
    translate([-7.5,-26,0.5*base_height])
        cube([12, 5, base_height], center=true);
    translate([10,-26,0.5*base_height])
        cube([10, 5, base_height], center=true);
    translate([12.5,0,0.5*base_height])
        cube([5, 52, base_height], center=true);
    translate([-12.5,0,0.5*base_height])
        cube([5, 52, base_height], center=true);
}

module NodeLeg()
{
    hull()
    {
        cylinder(d=8, h=base_height, $fn=64);
        cylinder(d=6, h=25,$fn=64);
    }
    if(node_type == 0) cylinder(d=2.8,h=28,$fn=64);
    if(node_type == 1) cylinder(d=3.2,h=28,$fn=64);
}

module ULN2003Holder()
{
    translate([-15, -13.5, 0])
        ULN2003Leg();
    translate([15, -13.5, 0])
        ULN2003Leg();
    translate([-15, 13.5, 0])
        ULN2003Leg();
    translate([15, 13.5, 0])
        ULN2003Leg();
    /*
    translate([0,13.5,0.5*base_height])
        cube([30, 5, base_height], center=true);
    translate([0,-13.5,0.5*base_height])
        cube([30, 5, base_height], center=true);
    translate([15,0,0.5*base_height])
        cube([5, 27, base_height], center=true);
    translate([-15,0,0.5*base_height])
        cube([5, 27, base_height], center=true);
    */
}

module ULN2003Leg()
{
    hull()
    {
        cylinder(d=8, h=base_height, $fn=64);
        cylinder(d=5, h=5,$fn=64);
    }
    cylinder(d=2.4,h=8,$fn=64);
}

/*
module SwitcherLoose()
{
	union()
	{
		difference()
		{
			hull()
			{
				cylinder(d=12, h=4, $fn=64);
				translate([10,-7,0])
				cube([12,14,4]);
			}
			translate([10,0,-0.1])
			cylinder(d=6,h=4.2,$fn=64);
			translate([10,-3,-0.1])
			cube([14,6,4.2]);
		}
		translate([22,5,0])
		cylinder(d=4,h=4,$fn=64);
		translate([22,-5,0])
		cylinder(d=4,h=4,$fn=64);
        
        cylinder(d=10,h=switcher_height,$fn=64);
		translate([0,0,14])
		difference()
		{
			translate([0,0,switcher_height])
			//rotate([0,0,20])
			rotate([180,0,0])
			StepperAxis();
			//rotate([0,0,-20])
			//rotate([180,0,0])
			//StepperAxis();
		}
	}
}*/

module SwitcherLoose()
{
    difference()
    {
        union()
        {
            difference()
            {
                hull()
                {
                    cylinder(d=12, h=4, $fn=64);
                    translate([10,-7,0])
                    cube([12,14,4]);
                }
                translate([10,0,-0.1])
                cylinder(d=6,h=4.2,$fn=64);
                translate([10,-3,-0.1])
                cube([14,6,4.2]);
            }
            
            translate([22,5,0])
            cylinder(d=4,h=4,$fn=64);
            translate([22,-5,0])
            cylinder(d=4,h=4,$fn=64);
            
            cylinder(d=10,h=switcher_height,$fn=64);	
        }
        union()
        {
            translate([0,0,switcher_height])
            rotate([0,0,20])
            rotate([180,0,0])
            StepperAxis();
            translate([0,0,switcher_height])
            rotate([0,0,-20])
            rotate([180,0,0])
            StepperAxis();
        }
	}
}

module Switcher()
{
    difference()
    {
        union()
        {
            difference()
            {
                hull()
                {
                    cylinder(d=12, h=4, $fn=64);
                    translate([10,-7,0])
                    cube([12,14,4]);
                }
                translate([10,0,-0.1])
                cylinder(d=6,h=4.2,$fn=64);
                translate([10,-3,-0.1])
                cube([14,6,4.2]);
            }
            
            translate([22,5,0])
            cylinder(d=4,h=4,$fn=64);
            translate([22,-5,0])
            cylinder(d=4,h=4,$fn=64);
            
            cylinder(d=10,h=switcher_height,$fn=64);	
        }
        translate([0,0,switcher_height])
        rotate([180,0,0])
        StepperAxis();
	}
}

module BasePlate()
{
    difference()
    {
        hull()
        {
            translate([-4*base_width+edge_offset,edge_offset,0])
            cube([8*base_width-2*edge_offset, 2*base_width-edge_offset, base_height]);
            translate([-3.5*base_width,5*base_width,0])
            cube([7*base_width, 1*base_width, base_height]);
        }
        translate([-3*base_width-0.35,2*base_width-0.3,-1])
        cube([6*base_width+0.7, base_width+0.6, base_height + 2]);
        translate([-2*base_width,4*base_width,-1])
        cube([4*base_width, 3*base_width, base_height + 2]);
    }
    translate([-4*base_width,0,base_height-1])
    makeStuds(8,2,stud_diameter);
    translate([2*base_width,3*base_width,base_height-1])
    makeStuds(1,3,stud_diameter);
    translate([-3*base_width-0.7,3*base_width,base_height-1])
    makeStuds(1,1,stud_diameter);
    translate([-3*base_width-1.5,4*base_width,base_height-1])
    makeStuds(1,1,stud_diameter);
    translate([-3*base_width-2.5,5*base_width,base_height-1])
    makeStuds(1,1,stud_diameter);  
}

module Bottom()
{
    union()
    {
        translate([4*base_width,0,0])
        BasePlate();
        translate([-8*base_width+edge_offset,edge_offset,0])
        cube([8*base_width + 1, 2*base_width-edge_offset, base_height]);
        translate([-8*base_width,0,base_height-1])
        makeStuds(8,2,stud_diameter);
        hull()
        {
            translate([-1.5*base_width-2, 8*base_width+1, 0])
            cylinder(d=10, h=base_height-1.6, $fn=64);
            translate([-1.5*base_width+2, 8*base_width-1, 0])
            cylinder(d=10, h=base_height-1.6, $fn=64);
        }
    }
  
    difference()
    {
        union()
        {
            translate([-20,48,0])
            rotate([0,0,-45])
            union()
            {
                cylinder(d=12, h=base_height+1, $fn=64);
                rotate([0,0,-25])
                translate([0,-2.5,0])
                cube([20,5,base_height]);
                rotate([0,0,180+25])
                translate([0,-2.5,0])
                cube([20,5,base_height]);
                rotate([0,0,110])
                translate([0,-2.5,0])
                cube([20,5,base_height-1.6]);
                hull()
                {
                    translate([-17.5,-8,0])
                    cylinder(d=10, h=base_height, $fn=64);
                    translate([-17.5,-8,0])
                    cylinder(d=8, h=motor_height, $fn=64);
                }
                hull()
                {
                    translate([17.5,-8,0])
                    cylinder(d=10, h=base_height, $fn=64);
                    translate([17.5,-8,0])
                    cylinder(d=8, h=motor_height, $fn=64);
                }
                
            }
        }
        translate([-20,48,0])
        rotate([0,0,-45])
        union()
        {
            translate([-17.5,-8,-1])
            cylinder(d=3.8, h=25, $fn=64);
            translate([-17.5,-8,-1])
            hull()
            {
                cylinder(d=3.8, h=7, $fn=64);
                cylinder(d=6, h=5, $fn=64);
            }
            translate([17.5,-8,-1])
            cylinder(d=3.8, h=25, $fn=64);
            translate([17.5,-8,-1])
            hull()
            {
                cylinder(d=3.8, h=7, $fn=64);
                cylinder(d=6, h=5, $fn=64);
            }       
        }  
    }
    
    translate([-8*base_width+edge_offset,edge_offset,0])
    cube([base_width-edge_offset,18*base_width-2*edge_offset,base_height]);
    translate([-8*base_width,0,base_height-1])
    makeStuds(1,18,stud_diameter);
    
    translate([-38,88,0])
    rotate([0,0,90])
    ULN2003Holder();
    translate([-60.5,70.5,0])
    cube([10,5,base_height]);
    translate([-60.5,100.5,0])
    cube([36.5,5,base_height]);
    translate([-27,70.5,0])
    cube([5,30,base_height]);
    translate([-37.5,59,0])
    rotate([0,0,-35])
    cube([5,17,base_height]);
    translate([-42.5,56,0])
    rotate([0,0,35])
    cube([5,17,base_height]);
            
    if(arduino_bottom)
    {
        translate([-17.6,16,0])
        cube([7,11,base_height]);
        difference()
        {
            translate([-7*base_width,2*base_width,0])
            cube([39,22,base_height]);
            translate([-7*base_width+1.7,2*base_width+1.8,1.4])
            cube([33.6,18.4,base_height]);
            translate([-7*base_width+2.1,2*base_width+2.2,-1])
            cube([32.8,17.6,3]);
            
            
        }      
    }
    else
    {
        translate([-15.6,16,0])
        cube([5,10,base_height]);
    }
}

module BottomWithMagnet()
{
    
    difference()
    {
        union()
        {
            Bottom();
            translate([4*base_width,base_width,0])
            hull()
            {
                translate([0,0,0.5*base_height])
                    cube([  2*base_width-2*edge_offset,
                            2*base_width-2*edge_offset,
                            base_height               ], center=true);
                translate([0,0,0.5*magnet_height])
                    cube([  2*base_width-2*edge_offset,
                            11,
                            magnet_height               ], center=true);
            }
        }
        translate([4*base_width,base_width,0.5*magnet_z+1+magnet_height-magnet_z])
        cube([magnet_x,magnet_y,magnet_z+2], center = true);
        
        //Recut the hole for shifter
        translate([1*base_width-0.35,2*base_width-0.3,-1])
        cube([6*base_width+0.7, base_width+0.6, base_height + 2]);
    }
}

module BottomWithSensorLow()
{
    
    difference()
    { 
        Bottom();
        union()
        {
            translate([-5.1*base_width-1,base_width-3.5, 1])
            cube([9.5*base_width-0.1, 7, 2*base_height]);
            translate([-5-4.5*base_width,7.5+base_width,1])
            rotate([0,0,180])
            intersection()
            {
                cube([20,20,2*base_height]);
                translate([0,0,-1])
                difference()
                {
                    cylinder(d=22,h=2*base_height+2,$fn=64);
                    translate([0,0,-2])
                    cylinder(d=8,h=2*base_height+4,$fn=64);
                }
            }
            translate([-15-4.625*base_width,2*base_width-(base_width-7.5)-0.1,1])
            cube([7,7,2*base_height]);
        }
    }
    
    
}


module StepperAxis()
{
	translate([0,0,-2])
	difference()
	{
		cylinder(d=5.4,h=10.6,$fn=64);
		translate([0,3+1.7,8])
		cube([10,6,8], center=true);
		translate([0,-(3+1.7),8])
		cube([10,6,8], center=true);
	}
}

module MotorTop()
{
    difference()
    {
        union()
        {
            translate([-5,3,0])
            rotate([0,0,45])
            {
                translate([-4,10.5,0])
                union()
                {
                    ConnectionInterface();
                    translate([0,0,0.5*base_height])
                    cube([42,5,base_height], center=true);
                }
            }
            cylinder(d=34, h=12, $fn=64);
            translate([-11.75,0,0])
            cube([23.5,20,12]);
        }
        translate([0,0,2])
        union()
        {
            translate([0,0,-3])
            cylinder(d=20, h=4,$fn=64);
            cylinder(d=28.4, h=17, $fn=64);
            translate([-7.5,0,0])
            cube([15,17,17]);
            translate([-8.75,0,0])
            cube([17.5,15,17]);
        }
    }
    
}

module ConnectionInterface()
{
    translate([25,0,0])
    difference()
    {
        cylinder(d=12, h=base_height, $fn=64);
        translate([0,0,-1])
        cylinder(d=3.8, h=base_height+2, $fn=64);
    }
    translate([-25,0,0])
    difference()
    {
        cylinder(d=12, h=base_height, $fn=64);
        translate([0,0,-1])
        cylinder(d=3.8, h=base_height+2, $fn=64);
    }
}

module MotorCut()
{
	union()
	{
		cylinder(d=29.2,h=25,$fn=64);
		translate([0,-10.6,12.5])
		cube([16,16,25], center=true);
		hull()
		{
			translate([17.5,0,0])
			cylinder(d=8, h=25, $fn=64);
			translate([-17.5,0,0])
			cylinder(d=8, h=25, $fn=64);
		}
		translate([17.5,0,-1])
		cylinder(d=3.8,h=50,$fn=64,center=true);
		translate([-17.5,0,-1])
		cylinder(d=3.8,h=50,$fn=64,center=true);
		//translate([0,8,-12])
		//cylinder(d=12,h=28,$fn=64,center=true);
	}
}

module magnetTest()
{
    difference()
    {
        translate([0,0,1.5])
        cube([65,10,3], center=true);
        translate([-7.5,0,2.5])
        cube([10,4.9,3], center=true);
        translate([-22.5,0,2.5])
        cube([10.1,5,3], center=true);
        translate([7.5,0,2.5])
        cube([10.2,5.1,3], center=true);
        translate([22.5,0,2.5])
        cube([10.3,5.2,3], center=true);
    }
}

module magnetBrick()
{
    difference()
    {
        union()
        {
            translate([0,0,0.5*base_height])
            cube([  8*base_width-2*edge_offset,
                    2*base_width-2*edge_offset,
                    base_height                 ], center = true);
            translate([-4*base_width,-base_width,base_height-1])
            makeStuds(3,2);
            translate([base_width,-base_width,base_height-1])
            makeStuds(3,2);        
            
            hull()
            {
                translate([0,0,0.5*base_height])
                    cube([  2*base_width-2*edge_offset,
                            2*base_width-2*edge_offset,
                            base_height               ], center=true);
                translate([0,0,0.5*magnet_height])
                    cube([  2*base_width-2*edge_offset,
                            11,
                            magnet_height               ], center=true);
            }
        }
        translate([0,0,0.5*magnet_z+1+magnet_height-magnet_z])
        cube([magnet_x,magnet_y,magnet_z+2], center = true);
    }   
}

module plateOfMagnetBricks(x, y)
{
    for(xPos=[0:1:(x-1)])
        for(yPos = [0:1:(y-1)])
            translate([xPos*9*base_width, yPos*3*base_width, 0])
                magnetBrick();
}


module roundMagnetBrick()
{
    magnet_height = 10;
    md = 8;
    mh = 1;
    translate([0,0,0.5*base_height])
    cube([8*base_width-2*edge_offset,2*base_width-2*edge_offset, base_height], center = true);
    translate([-4*base_width,-base_width,base_height-1])
    makeStuds(3,2);
    translate([base_width,-base_width,base_height-1])
    makeStuds(3,2);
    
    difference()
    {
        translate([0,0,0.5*magnet_height])
        cube([2*base_width-2*edge_offset,2*base_width-2*edge_offset, magnet_height], center=true);
        translate([0,0,-0.5*mh+0.5+magnet_height])
        cylinder(d=md, h=mh+1, $fn=64, center=true);
    } 
}

/*
module hallSensorBrick()
{
    hallSensorX=5;
    hallSensorY=5;
    hallSensorZ=2;
    wireWidth=3;
    difference()
    {
        translate([0,0,2.5])
        cube([2*base_width, 4*base_width, 5], center=true);
        translate([0,1.5*base_width,0.5*hallSensorZ+2.5+1])
        cube([hallSensorX, hallSensorY, hallSensorZ + 5], center=true);
        translate([0,-1.5*base_width,0.5*hallSensorZ++2.5+1])
        cube([hallSensorX, hallSensorY, hallSensorZ + 5], center=true);
        translate([0,0,2.5+1])
        cube([wireWidth, 3*base_width, 5],center=true);
        translate([-4.5,6,-1])
        cylinder(d=3.6, h=7, $fn=128);
        translate([-4.5,6,-1])
        cylinder(d=6, h=4.6, $fn=128);
        translate([4.5,-6,-1])
        cylinder(d=3.6, h=7, $fn=128);
        translate([4.5,-6,-1])
        cylinder(d=6, h=4.6, $fn=128);
    }
}*/

module hallSensorBrickHigh()
{
    difference()
    {
        union()
        {
            makeStandardLegoBrick(8,2,1);
            makeStandardLegoBrick(4,2,2);
        }
        translate([-4*base_width,-0.25*base_width,1])
        hull()
        {
            cube([2*base_width,5,3*base_height]);
            translate([base_width,0,base_height])
            cube([2*base_width,5,3*base_height]);
        }
        translate([-2*base_width,-0.25*base_width,base_height+1])
        cube([2.5*base_width,5,3*base_height]);      
    }  
}

module hallSensorBrickLow()
{
    difference()
    {
        makeStandardLegoBrick(8,2,1);
        translate([-4.5*base_width,-0.25*base_width,1])
        cube([5*base_width,5,2*base_height]);     
    }  
}

module hallSensorBrickBoogie()
{
    difference()
    {
        
        union()
        {
            
            makeStandardLegoBrick(4,3,1,false);
            translate([0,1*base_width,0])
            makeStandardLegoBrick(4,1,3,true);
            translate([0,-1*base_width,0])
            makeStandardLegoBrick(4,1,3,true);
        }
        
        translate([3.5,-0.5*base_width-2,base_width-1.6])
        rotate([90,0,-90])
        difference()
        {
            translate([-base_width,-2*base_height,0])
            cube([2*base_width, 3*base_width,7]);
            translate([0.5*base_width,-0.5*base_width,-1])
            union()
            {
                cylinder(d=base_width,h=9,$fn=64);
                translate([0,-1.5*base_width,0])
                cube([2*base_width,2*base_width,9]);
                translate([-0.5*base_width,-2*base_width,0])
                cube([2*base_width,2*base_width,9]);
            }
        }
    }
}



module hallSensorBrickBoogieDouble()
{
    
    difference()
    {
        makeStandardLegoBrick(4,6,3,true);
        union()
        {
            translate([0,-1.5*base_width,0])
            hallSensorBrickBoogieDoubleCutOut();
            translate([0,1.5*base_width,0])
            rotate([0,0,180])
            hallSensorBrickBoogieDoubleCutOut();
            magnetHoleBottom();
        }   
    }
}

module hallSensorBrickBoogieDoubleCutOut()
{
    translate([0,0,base_height-0.2])
    translate([0,0,1.5*base_height])
    cube([5*base_width,base_width+0.4,3*base_height], center=true);
    translate([3.5,-0.5*base_width-2,base_width-1.6])
    rotate([90,0,-90])
    difference()
    {
        translate([-base_width,-2*base_height,0])
        cube([2*base_width, 3*base_width,7]);
        translate([0.6*base_width,-0.6*base_width,-1])
        union()
        {
            cylinder(d=base_width,h=9,$fn=64);
            translate([0,-1.5*base_width,0])
            cube([2*base_width,2*base_width,9]);
            translate([-0.5*base_width,-2*base_width,0])
            cube([2*base_width,2*base_width,9]);
        }
    }
}

module magnetHoleBottom()
{
    translate([0,0,0.5*magnet_z-0.5])
    cube([magnet_x,magnet_y,magnet_z+1], center=true);
    translate([0,0,magnet_z])
    rotate([0,90,0])
    cylinder(d=magnet_y, h=magnet_x, $fn=64, center=true);
}




module hallSensorBrickBoogieWithMagnet()
{
    
    difference()
    {
        translate([0,-1.5*base_width,0])
        hallSensorBrickBoogie();
        hull()
        {
            translate([0,0,0.5*magnet_z+1-2])
            cube([magnet_x,magnet_y,magnet_z+2], center = true);
            translate([0,0.25*magnet_y,8])
            cube([magnet_x,0.5*magnet_y,1], center=true);
        }
    }
   
}



module makeStandardLegoBrick(x,y,z,studs=true)
{
    translate([0,0,0.5*(z*base_height-z_offset)])
    cube([  x*base_width-2*edge_offset,
            y*base_width-2*edge_offset,
            z*base_height-z_offset],
            center=true             );
    if(studs)
    {
        translate([-0.5*x*base_width,-0.5*y*base_width,z*base_height-1])
        makeStuds(x,y);
    }
}

module makeStuds(x, y, dia = stud_diameter)
{
    for(xPos=[stud_offset:base_width:stud_offset + (x-1) * base_width])
        for(yPos = [stud_offset:base_width:stud_offset + (y-1) * base_width])
            translate([xPos, yPos, 0])
                cylinder(d = dia, h = stud_height+1, $fn=64);
}

module makeStandardLegoBrickBottom(x,y,z)
{
    
    difference()
    {
        translate([0,0,0.5*(z*base_height-z_offset)])
        cube([  x*base_width-2*edge_offset,
                y*base_width-2*edge_offset,
                z*base_height-z_offset],
                center=true             );
        translate([0,0,0.5*(base_height) + z*base_height - stud_height - 0.2])
        cube([  x*base_width-2*edge_width,
                y*base_width-2*edge_width,
                base_height],
                center=true             );
    }
    
    translate([-0.5*x*base_width,-0.5*y*base_width,(z-1)*base_height-z_offset])
    makeLegoBottomRings(x, y);
}

module makeLegoBottomRings(x, y, dia_outer = ring_diameter_outer, dia_inner = ring_diameter_inner)
{
    for(xPos=[base_width:base_width:base_width + (x-2) * base_width])
        for(yPos = [base_width:base_width:base_width + (y-2) * base_width])
            translate([xPos, yPos, 0.5])
            difference()
            {
                cylinder(d = dia_outer, h = base_height-0.5, $fn=64);
                translate([0,0,-1.5])
                cylinder(d = dia_inner, h = base_height+2, $fn=64);
            }   
}