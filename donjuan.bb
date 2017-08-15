Type Entity
    Field id%
    Field health%
    Field x#,y#
    Field sector.Sector
    Field angle#
    Field time%
    Field anim_time%
    Field frame%
    Field updated%
    Field unique_id%
End Type

Type Sector
    Field id%
    Field num_occupants%
    Field occupants.Entity
End Type

Const MAP_WIDTH = 80
Const MAP_HEIGHT = 40

Const MOTION_DIRECTION_NORTH = 1
Const MOTION_DIRECTION_NORTH_EAST = 2
Const MOTION_DIRECTION_EAST = 3
Const MOTION_DIRECTION_SOUTH_EAST = 4
Const MOTION_DIRECTION_SOUTH = 5
Const MOTION_DIRECTION_SOUTH_WEST = 6
Const MOTION_DIRECTION_WEST = 7
Const MOTION_DIRECTION_NORTH_WEST = 8

Const ENTITY_ID_GENERATOR = -1
Const ENTITY_ID_GHOST = -2
Const ENTITY_ID_FIREBALL = -3
Const ENTITY_ID_RED_KEY = -4
Const ENTITY_ID_GREEN_KEY = -5
Const ENTITY_ID_RED_DOOR_HORIZ = -6
Const ENTITY_ID_GREEN_DOOR_HORIZ = -7
Const ENTITY_ID_RED_DOOR_VERT = -8
Const ENTITY_ID_GREEN_DOOR_VERT = -9
Const ENTITY_ID_EXIT = -10

Const KEY_UP = 200
Const KEY_LEFT = 203
Const KEY_RIGHT = 205
Const KEY_DOWN = 208
Const KEY_LEFT_CONTROL = 29
Const KEY_SPACE = 57
Const KEY_I = 23
Const KEY_O = 24

Global SCALE# = 2.0
Global UNIQUE_ID = 0

Global GRAPHICS_WIDTH = 1024
Global GRAPHICS_HEIGHT = 768

Graphics GRAPHICS_WIDTH, GRAPHICS_HEIGHT,0,0

Global SCROLL_POSITION_LEFT# = GRAPHICS_WIDTH * (1.0 / 3.0) / SCALE#
Global SCROLL_POSITION_RIGHT# = GRAPHICS_WIDTH * (2.0 / 3.0) / SCALE#
Global SCROLL_POSITION_UP# = GRAPHICS_HEIGHT * (1.0 / 3.0) / SCALE#
Global SCROLL_POSITION_DOWN# = GRAPHICS_HEIGHT * (2.0 / 3.0) / SCALE#

Global MAP_WIDTH_PIXELS = MAP_WIDTH * 32
Global MAP_HEIGHT_PIXELS = MAP_HEIGHT * 32

Global AXIS_REMAINDER_X = GRAPHICS_WIDTH Mod Int(32 * SCALE#)
Global AXIS_REMAINDER_Y = GRAPHICS_HEIGHT Mod Int(32 * SCALE#)

Global AXIS_REMAINDER_X_EXISTS = AXIS_REMAINDER_X <> 0
Global AXIS_REMAINDER_Y_EXISTS = AXIS_REMAINDER_Y <> 0

Global NUM_VISIBLE_SECTORS_X = GRAPHICS_WIDTH / Int(32 * SCALE#) + AXIS_REMAINDER_X_EXISTS
Global NUM_VISIBLE_SECTORS_Y = GRAPHICS_HEIGHT / Int(32 * SCALE#) + AXIS_REMAINDER_Y_EXISTS

Global SCROLL_LIMIT_X = MAP_WIDTH_PIXELS - GRAPHICS_WIDTH / SCALE#
Global SCROLL_LIMIT_Y = MAP_HEIGHT_PIXELS - GRAPHICS_HEIGHT / SCALE#

Global G_SPEED# = 1.0 ; number of cycles per second, must be > 0.0
Global G_UPDATE_TIME = 1000.0 / G_SPEED# ; time taken for one complete cycle
Global G_PREV_TIME = 0 ; records the last time the gradient was at phase 0.0

Global G_R1 = 0
Global G_G1 = 255
Global G_B1 = 0

Global G_R2 = 255
Global G_G2 = 255
Global G_B2 = 255

Global G_RDIFF = G_R2 - G_R1
Global G_GDIFF = G_G2 - G_G1
Global G_BDIFF = G_B2 - G_B1

AutoMidHandle False

Dim TILES(4)

TILES(1) = LoadImage("wall_top_horiz_red.png")
TILES(2) = LoadImage("wall_bottom_horiz_red.png")
TILES(3) = LoadImage("wall_top_vert_red.png")
TILES(4) = LoadImage("wall_bottom_vert_red.png")
Global FLOOR_TILES = LoadImage("tiles.png")

MaskImage TILES(1), 255,0,255
MaskImage TILES(2), 255,0,255
MaskImage TILES(3), 255,0,255
MaskImage TILES(4), 255,0,255

Global IMG_WIZARD = LoadAnimImage("wizard.png",64,64,0,8)
Global IMG_GENERATOR = LoadAnimImage("generator.png",64,64,0,3)
Global IMG_GHOST = LoadAnimImage("ghostie.png",64,64,0,3)
Global IMG_FIREBALL = LoadImage("fireball.png")
Global IMG_GREEN_KEY = LoadImage("green_key.png")
Global IMG_RED_KEY = LoadImage("red_key.png")
Global IMG_HEALTH_BAR = LoadImage("health_bar.png")

MaskImage IMG_WIZARD, 255,0,255
MaskImage IMG_GENERATOR, 255,0,255
MaskImage IMG_GHOST, 255,0,255
MaskImage IMG_FIREBALL, 255,0,255
MaskImage IMG_GREEN_KEY, 255,0,255
MaskImage IMG_RED_KEY, 255,0,255

Global SND_GHOST = LoadSound("./ghost.wav")
Global SND_FIREBALL = LoadSound("./fireball.wav")

Global FONT = LoadFont("./UbuntuMono-B.ttf",GRAPHICS_WIDTH / 32,True,False,False)
SetFont FONT
Global FONT_WIDTH = StringWidth("0")
Global FONT_HEIGHT = StringHeight("0")
Global HUD_WIDTH = 8 * FONT_WIDTH + 8
Global HUD_HEIGHT = 3 * FONT_HEIGHT + 8

Global SECTOR_ID%

Global CURRENT_TIME% = Millisecs()

Global ANIM_TIME = CURRENT_TIME%
Global SHOOT_TIME = CURRENT_TIME%
Global WIZARD_FRAME = 0
Global SCORE = 0
Global WIZARD_X# = 64,WIZARD_Y# = 64,WIZARD_SPEED# = 3.5,WIZARD_HEALTH# = 1.0,FIREBALL_SPEED# = 8.0
Global WIZARD_NUM_RED_KEYS% = 0
Global WIZARD_NUM_GREEN_KEYS% = 0
Global SCROLL_X# = 0.0,SCROLL_Y# = 0.0
Global WIZARD_SCREEN_X# = 64,WIZARD_SCREEN_Y# = 64
Global WIZARD_TRANSLATION_X# = 0.0
Global WIZARD_TRANSLATION_Y# = 0.0

Global GHOST_SPEED# = 2.0

Global TILES_REMAINDER_X = 0,TILES_REMAINDER_Y = 0

Global WIZARD_DIRECTION = 0
Global MOTION_DIRECTION = MOTION_DIRECTION_SOUTH

SetScale SCALE#,SCALE#

Global TIMER = CreateTimer(30)

Dim MAP.Sector(MAP_WIDTH,MAP_HEIGHT)
LoadMap

While Not KeyDown(1)

    Frames = WaitTimer(TIMER)
    
    For i = 1 To Frames
    
        Cls
        
        CURRENT_TIME% = Millisecs()
        
        UpdateWizard
        Shoot
        UpdateEnemies
        ClampScrolling
        
        DrawTiles
        DrawMap
        DrawWizard
        DrawHUD
        
        ClearEntityUpdateFlags
    
    Flip
    
    Next
    
Wend

End

Function ClearEntityUpdateFlags()
    Local e.Entity
    For e = Each Entity
        e\updated = 0
    Next
End Function

Function LoadMap()

    Restore MapData
    
    For y = 0 To MAP_HEIGHT- 1
    
    
        For x = 0 TO MAP_WIDTH - 1
            MAP(x,y) = New Sector
            Read SECTOR_ID%
            MAP(x,y)\id% = SECTOR_ID%
        
            If SECTOR_ID% > 0
                MAP(x,y)\id% = SECTOR_ID%
                Else If SECTOR_ID% < 0
                MAP(x,y)\id% = SECTOR_ID%;0
                MAP(x,y)\num_occupants = MAP(x,y)\num_occupants + 1
                Select SECTOR_ID%
                    Case ENTITY_ID_GENERATOR
                        MAP(x,y)\occupants = CreateGenerator(x * 32,y * 32)
                    Case ENTITY_ID_GHOST
                        MAP(x,y)\occupants = CreateGhost(x * 32,y * 32)
                    Case ENTITY_ID_RED_KEY
                        MAP(x,y)\occupants = CreateRedKey(x * 32,y * 32)
                    Case ENTITY_ID_GREEN_KEY
                        MAP(x,y)\occupants = CreateGreenKey(x * 32,y * 32)
                    Case ENTITY_ID_RED_DOOR_HORIZ
                        MAP(x,y)\occupants = CreateRedDoorHoriz(x * 32,y * 32)
                    Case ENTITY_ID_GREEN_DOOR_HORIZ
                        MAP(x,y)\occupants = CreateGreenDoorHoriz(x * 32,y * 32)
                    Case ENTITY_ID_RED_DOOR_VERT
                        MAP(x,y)\occupants = CreateRedDoorVert(x * 32,y * 32)
                    Case ENTITY_ID_GREEN_DOOR_VERT
                        MAP(x,y)\occupants = CreateGreenDoorVert(x * 32,y * 32)
                    Case ENTITY_ID_EXIT
                        MAP(x,y)\occupants = CreateExit(x * 32,y * 32)
                End Select
                
            End If
        Next
    Next
    
End Function

Function CreateGenerator.Entity(x%,y%)
    Local generator.Entity = New Entity
    generator\id% = ENTITY_ID_GENERATOR
    generator\health% = 3
    generator\x# = x
    generator\y# = y
    generator\sector = MAP(x / 32,y / 32)
    generator\frame% = 0
    generator\time% = CURRENT_TIME
    generator\unique_id = UNIQUE_ID
    UNIQUE_ID = UNIQUE_ID + 1
    ;Print "Created generator with UNIQUE_ID " + generator\unique_id
    Return generator
End Function

Function CreateGhost.Entity(x%,y%)
    Local ghost.Entity = New Entity
    ghost\id% = ENTITY_ID_GHOST
    ghost\health% = 3
    ghost\x# = x
    ghost\y# = y
    ghost\sector = MAP(x / 32,y / 32)
    ghost\frame% = 0
    ghost\time% = CURRENT_TIME
    ghost\unique_id = UNIQUE_ID
    UNIQUE_ID = UNIQUE_ID + 1
    ;Print "Created ghost with UNIQUE_ID " + ghost\unique_id
    Return ghost
End Function

Function CreateRedKey.Entity(x%,y%)
    Local red_key.Entity = New Entity
    red_key\id% = ENTITY_ID_RED_KEY
    red_key\x# = x
    red_key\y# = y
    red_key\sector = MAP(x / 32,y / 32)
    red_key\unique_id = UNIQUE_ID
    UNIQUE_ID = UNIQUE_ID + 1
    ;Print "Created red key with UNIQUE_ID " + red_key\unique_id
    Return red_key
End Function

Function CreateGreenKey.Entity(x%,y%)
    Local green_key.Entity = New Entity
    green_key\id% = ENTITY_ID_GREEN_KEY
    green_key\x# = x
    green_key\y# = y
    green_key\sector = MAP(x / 32,y / 32)
    green_key\unique_id = UNIQUE_ID
    UNIQUE_ID = UNIQUE_ID + 1
    ;Print "Created green key with UNIQUE_ID " + green_key\unique_id
    Return green_key
End Function

Function CreateRedDoorHoriz.Entity(x%,y%)
    Local red_door.Entity = New Entity
    red_door\id% = ENTITY_ID_RED_DOOR_HORIZ
    red_door\x# = x
    red_door\y# = y
    red_door\sector = MAP(x / 32,y / 32)
    red_door\unique_id = UNIQUE_ID
    UNIQUE_ID = UNIQUE_ID + 1
    ;Print "Created red door with UNIQUE_ID " + red_door\unique_id
    Return red_door
End Function

Function CreateGreenDoorHoriz.Entity(x%,y%)
    Local green_door.Entity = New Entity
    green_door\id% = ENTITY_ID_GREEN_DOOR_HORIZ
    green_door\x# = x
    green_door\y# = y
    green_door\sector = MAP(x / 32,y / 32)
    green_door\unique_id = UNIQUE_ID
    UNIQUE_ID = UNIQUE_ID + 1
    ;Print "Created green door with UNIQUE_ID " + green_door\unique_id
    Return green_door
End Function

Function CreateRedDoorVert.Entity(x%,y%)
    Local red_door.Entity = New Entity
    red_door\id% = ENTITY_ID_RED_DOOR_VERT
    red_door\x# = x
    red_door\y# = y
    red_door\sector = MAP(x / 32,y / 32)
    red_door\unique_id = UNIQUE_ID
    UNIQUE_ID = UNIQUE_ID + 1
    ;Print "Created red door with UNIQUE_ID " + red_door\unique_id
    Return red_door
End Function

Function CreateGreenDoorVert.Entity(x%,y%)
    Local green_door.Entity = New Entity
    green_door\id% = ENTITY_ID_GREEN_DOOR_VERT
    green_door\x# = x
    green_door\y# = y
    green_door\sector = MAP(x / 32,y / 32)
    green_door\unique_id = UNIQUE_ID
    UNIQUE_ID = UNIQUE_ID + 1
    ;Print "Created green door with UNIQUE_ID " + green_door\unique_id
    Return green_door
End Function

Function CreateFireball.Entity(x%,y%,sector.Sector,angle#)
    Local fireball.Entity = New Entity
    fireball\id% = ENTITY_ID_FIREBALL
    fireball\health% = 1
    fireball\x# = x
    fireball\y# = y
    fireball\sector = sector
    fireball\angle# = angle#
    fireball\time% = CURRENT_TIME
    fireball\unique_id = UNIQUE_ID
    UNIQUE_ID = UNIQUE_ID + 1
    ;Print "Created fireball with UNIQUE_ID " + fireball\unique_id
    Return fireball
End Function

Function CreateExit.Entity(x%,y%)
    Local exit_.Entity = New Entity
    exit_\id% = ENTITY_ID_EXIT
    exit_\health% = 1
    exit_\x# = x
    exit_\y# = y
    exit_\sector = MAP(x / 32,y / 32)
    exit_\time% = CURRENT_TIME
    exit_\unique_id = UNIQUE_ID
    UNIQUE_ID = UNIQUE_ID + 1
    ;Print "Created exit_ with UNIQUE_ID " + exit_\unique_id
    Return exit_
End Function

Function AddEntityToMap(e.Entity,sector_x,sector_y)
    If MAP(sector_x,sector_y)\occupants = Null
        MAP(sector_x,sector_y)\occupants = e
    Else
        Insert e Before MAP(sector_x,sector_y)\occupants
        MAP(sector_x,sector_y)\occupants = e
    End If
    e\sector = MAP(sector_x,sector_y)
    MAP(sector_x,sector_y)\num_occupants = MAP(sector_x,sector_y)\num_occupants + 1
End Function

Function TransplantEntity(e.Entity,o.Sector,n.Sector)

    If e = o\occupants
        o\occupants = After e
        If n\occupants = Null
            n\occupants = e
        Else
            Insert e Before n\occupants
            n\occupants = e
        End If
        Else
            If n\occupants = Null
            Insert e Before First Entity
            n\occupants = e
        Else
            Insert e Before n\occupants
            n\occupants = e
        End If
    End If
    
    o\num_occupants = o\num_occupants - 1
    n\num_occupants = n\num_occupants + 1
    
End Function

Function RemoveEntityFromMap(e.Entity)

    Local s.Sector
    
    s = e\sector
    If e = s\occupants
        If s\num_occupants > 1
            s\occupants = After e
        Else
            s\occupants = Null
        End If
    End If
    
    s\num_occupants = s\num_occupants - 1
    Insert e After Last Entity
    e\sector = Null
    
End Function

Function UpdateEnemies()

    Local o.Entity
    Local o2.Entity
    
    offset_x = Int(SCROLL_X#) / 32
    offset_y = Int(SCROLL_Y#) / 32
    
    For y = 0 To NUM_VISIBLE_SECTORS_Y
    
        For x = 0 to NUM_VISIBLE_SECTORS_X
            If x + offset_x < MAP_WIDTH And y + offset_y < MAP_HEIGHT
                If MAP(x + offset_x,y + offset_y)\num_occupants > 0
                    num_occupants = MAP(x + offset_x,y + offset_y)\num_occupants% - 1
                    o = MAP(x + offset_x,y + offset_y)\occupants
                    
                    i = 0
                    While i <= num_occupants And o <> Null
                    
                        If Not o\updated

                            Select o\id%
                                Case ENTITY_ID_FIREBALL
                                    o2 = UpdateFireball(o)
                                
                                Case ENTITY_ID_GENERATOR						
                                    o2 = UpdateGenerator(o)
        
                                Case ENTITY_ID_GHOST
                                    o2 = UpdateGhost(o)
                                
                            End Select
                            o\updated = 1
                        End If
                        
                        o = o2
                        i = i + 1
                    Wend
                End If
            End If
        Next
    Next
End Function

Function UpdateFireball.Entity(e.Entity)
    Local result.Entity
    
    result = After e
    
    old_sector_x = Int(e\x) / 32
    old_sector_y = Int(e\y) / 32
    e\x# = e\x# + FIREBALL_SPEED * Cos(e\angle#)
    e\y# = e\y# + FIREBALL_SPEED * Sin(e\angle#)
    new_sector_x = Int(e\x) / 32
    new_sector_y = Int(e\y) / 32
    
    If new_sector_x >= MAP_WIDTH Or new_sector_y >= MAP_HEIGHT Or new_sector_x < 0 Or new_sector_y < 0;e\x# <= 0.0 Or e\y# <= 0.0 Or e\x# >= MAP_WIDTH * 32 Or e\y# >= MAP_HEIGHT * 32
        RemoveEntityFromMap e
        Delete e
        Return result
    End If
    
    If old_sector_x <> new_sector_x Or old_sector_y <> new_sector_y
        RemoveEntityFromMap(e)
        AddEntityToMap(e,new_sector_x,new_sector_y)
    End If
    
    Return result

End Function


Function UpdateGhost.Entity(e.Entity)

    Local result.Entity
    
    result = After e
    
    Local old_sector_x%,old_sector_y%
    Local new_sector_x%,new_sector_y%
    Local x%,y%
    Local i%
    Local o.Entity
    Local dead_again% = False
    Local ghost_frame%
    
    old_sector_x = Int(e\x) / 32
    old_sector_y = Int(e\y) / 32
    
    e\angle# = ATan2(WIZARD_Y# - e\y,WIZARD_X# - e\x)
    e\x = e\x + GHOST_SPEED# * Cos(e\angle#)
    e\y = e\y + GHOST_SPEED# * Sin(e\angle#)
    
    new_sector_x = Int(e\x) / 32
    new_sector_y = Int(e\y) / 32
    
    If old_sector_x <> new_sector_x Or old_sector_y <> new_sector_y
        RemoveEntityFromMap(e)
        AddEntityToMap(e,new_sector_x,new_sector_y)
    End If
    
    If CURRENT_TIME - e\anim_time% > 300	
        e\anim_time% = CURRENT_TIME
        e\frame% = (e\frame% + 1) Mod 4
    End If
    
    For y% = new_sector_y% To new_sector_y% + 2
        For x% = new_sector_x% To new_sector_x% + 2
            If x% >= 0 And x% < MAP_WIDTH And y% >= 0 And y% < MAP_HEIGHT
                o = MAP(x%,y%)\occupants
                i = 0
                While i < MAP(x%,y%)\num_occupants% And dead_again% = False
                    If o\id% = ENTITY_ID_FIREBALL
                        If e\frame% < 3
                            ghost_frame% = e\frame%
                        Else
                            ghost_frame% = 1
                        End If	     
                        
                        If ImagesCollide(IMG_GHOST,e\x,e\y,ghost_frame%,IMG_FIREBALL,o\x,o\y,0)
                            PlaySound(SND_GHOST)
                            RemoveEntityFromMap(e)
                            Delete e
                            result = After o
                            RemoveEntityFromMap(o)
                            Delete o
                            dead_again% = True
                            SCORE = SCORE + 25
                            Return result
                        End If
                    End If
                    i = i + 1
                    o = After o
                Wend
            If dead_again% Then Exit
            End If
        Next
        
        If dead_again% Then Exit
        
    Next
    Return result
End Function

Function UpdateGenerator.Entity(e.Entity)
    Local result.Entity
    result = After e
    Local x%,y%
    Local i%
    Local o.Entity
    Local dead_again% = False
    
    sector_x = Int(e\x) / 32
    sector_y = Int(e\y) / 32
    
    If CURRENT_TIME - e\time% > 5000
        offset_x# = e\x# - WIZARD_X#
        offset_y# = e\y# - WIZARD_Y#
        distance# = Sqr(offset_x# * offset_x# + offset_y# * offset_y#)
        If distance# < 32 * 8
            e\time% = CURRENT_TIME
            AddEntityToMap(CreateGhost(e\x,e\y),sector_x,sector_y)
        End If
    End If
    
    For y% = sector_y% To sector_y% + 2
        For x% = sector_x% To sector_x% + 2
            If x% >= 0 And x% < MAP_WIDTH And y% >= 0 And y% < MAP_HEIGHT
            o = MAP(x%,y%)\occupants
            i = 0
            While i < MAP(x%,y%)\num_occupants% And dead_again% = False
                If o\id% = ENTITY_ID_FIREBALL	     
                
                    If ImagesCollide(IMG_GENERATOR,e\x,e\y,e\frame,IMG_FIREBALL,o\x,o\y,0)
                    
                        If e\health% = 1
                            RemoveEntityFromMap(e)
                            Delete e
                            result = After o
                            RemoveEntityFromMap(o)
                            Delete o
                            SCORE = SCORE + 75
                            dead_again% = True
                            Return result
                        Else
                            e\health% = e\health% - 1
                            e\frame% = e\frame% + 1
                            result = After o
                            RemoveEntityFromMap(o)
                            Delete o
                            dead_again% = True
                            Return result
                        End If
                    End If
                End If
                i = i + 1
                o = After o
            Wend
            If dead_again% Then Exit
            End If
        Next
            
        If dead_again% Then Exit
        
    Next
    
    Return result
End Function

Function AdjustScale()
    SCROLL_POSITION_LEFT# = GRAPHICS_WIDTH * (1.0 / 3.0) / SCALE#
    SCROLL_POSITION_RIGHT# = GRAPHICS_WIDTH * (2.0 / 3.0) / SCALE#
    SCROLL_POSITION_UP# = GRAPHICS_HEIGHT * (1.0 / 3.0) / SCALE#
    SCROLL_POSITION_DOWN# = GRAPHICS_HEIGHT * (2.0 / 3.0) / SCALE#
    
    MAP_WIDTH_PIXELS = MAP_WIDTH * 32
    MAP_HEIGHT_PIXELS = MAP_HEIGHT * 32
    
    AXIS_REMAINDER_X = GRAPHICS_WIDTH Mod Int(32 * SCALE#)
    AXIS_REMAINDER_Y = GRAPHICS_HEIGHT Mod Int(32 * SCALE#)
    
    AXIS_REMAINDER_X_EXISTS = AXIS_REMAINDER_X <> 0
    AXIS_REMAINDER_Y_EXISTS = AXIS_REMAINDER_Y <> 0
    
    NUM_VISIBLE_SECTORS_X = GRAPHICS_WIDTH / Int(32 * SCALE#) + AXIS_REMAINDER_X_EXISTS
    NUM_VISIBLE_SECTORS_Y = GRAPHICS_HEIGHT / Int(32 * SCALE#) + AXIS_REMAINDER_Y_EXISTS
    
    SCROLL_LIMIT_X = MAP_WIDTH_PIXELS - GRAPHICS_WIDTH / SCALE#
    SCROLL_LIMIT_Y = MAP_HEIGHT_PIXELS - GRAPHICS_HEIGHT / SCALE#
    ClampScrolling
    
    WIZARD_SCREEN_X = WIZARD_X - SCROLL_X
    WIZARD_SCREEN_Y = WIZARD_Y - SCROLL_Y
End Function

Function ScaleUp()
    SCALE# = SCALE# + 0.1
    AdjustScale
End Function

Function ScaleDown()
    SCALE# = SCALE# - 0.1
    AdjustScale
End Function

Function UpdateWizard()
    Local wizard_sector_x%,wizard_sector_y%
    Local x%,y%
    Local i%
    Local o.Entity
    Local dead_again% = False
    Local ghost_frame%
    
    If CURRENT_TIME - ANIM_TIME > 500
        WIZARD_FRAME = Not WIZARD_FRAME
        ANIM_TIME = CURRENT_TIME
    End If
    
    If KeyHit(KEY_I) And SCALE# < 4.0
        ScaleUp
    End If
    
    If KeyHit(KEY_O) And SCALE# > 1.0
        ScaleDown
    End If
    
    If KeyDown(KEY_UP) And Keydown(KEY_LEFT)
        WIZARD_DIRECTION = 1
        MOTION_DIRECTION = MOTION_DIRECTION_NORTH_WEST
        
        TranslateWizard(WIZARD_SPEED# * Cos(225),WIZARD_SPEED# * Sin(225))
        
        If Not ResolveWallCollisions()
            If SCROLL_Y# = 0.0 Or WIZARD_SCREEN_Y# > SCROLL_POSITION_UP#
                TranslateWizardScreen(0.0,WIZARD_SPEED# * Sin(225))
            Else
                Scroll(0.0,WIZARD_SPEED# * Sin(225))
            End If
        
            If SCROLL_X# = 0.0 Or WIZARD_SCREEN_X# > SCROLL_POSITION_LEFT#
                TranslateWizardScreen(WIZARD_SPEED# * Cos(225),0.0)
            Else
                Scroll(WIZARD_SPEED# * Cos(225),0.0)
            End If
        End If
    
    Else If KeyDown(KEY_UP) And Keydown(KEY_RIGHT)
    
        WIZARD_DIRECTION = 1
        MOTION_DIRECTION = MOTION_DIRECTION_NORTH_EAST
        
        TranslateWizard(WIZARD_SPEED# * Cos(315),WIZARD_SPEED# * Sin(315))
        If Not ResolveWallCollisions()
            If SCROLL_Y# = 0.0 Or WIZARD_SCREEN_Y# > SCROLL_POSITION_UP#
                TranslateWizardScreen(0.0,WIZARD_SPEED# * Sin(315))
            Else
                Scroll(0.0,WIZARD_SPEED# * Sin(315))
            End If
        
            If SCROLL_X# > SCROLL_LIMIT_X Or WIZARD_SCREEN_X# < SCROLL_POSITION_RIGHT#
                TranslateWizardScreen(WIZARD_SPEED# * Cos(315),0.0)
            Else
                Scroll(WIZARD_SPEED# * Cos(315),0.0)
            End If
        End If
    
    Else If KeyDown(KEY_DOWN) And Keydown(KEY_LEFT)
    
        WIZARD_DIRECTION = 0
        MOTION_DIRECTION = MOTION_DIRECTION_SOUTH_WEST
        
        TranslateWizard(WIZARD_SPEED# * Cos(135),WIZARD_SPEED# * Sin(135))
        
        If Not ResolveWallCollisions()
            If SCROLL_Y# >= SCROLL_LIMIT_Y Or WIZARD_SCREEN_Y# < SCROLL_POSITION_DOWN#
                TranslateWizardScreen(0.0,WIZARD_SPEED# * Sin(135))
            Else
                Scroll(0.0,WIZARD_SPEED# * Sin(135))
            End If
        
            If SCROLL_X# = 0.0 Or WIZARD_SCREEN_X# > SCROLL_POSITION_LEFT#
                TranslateWizardScreen(WIZARD_SPEED# * Cos(135),0.0)
            Else
                Scroll(WIZARD_SPEED# * Cos(135),0.0)
            End If
        End If
    Else If KeyDown(KEY_DOWN) And Keydown(KEY_RIGHT)
    
        WIZARD_DIRECTION = 0
        MOTION_DIRECTION = MOTION_DIRECTION_SOUTH_EAST
        
        TranslateWizard(WIZARD_SPEED# * Cos(45),WIZARD_SPEED# * Sin(45))
        If Not ResolveWallCollisions()
            If SCROLL_Y# >= SCROLL_LIMIT_Y Or WIZARD_SCREEN_Y# < SCROLL_POSITION_DOWN#
                TranslateWizardScreen(0.0,WIZARD_SPEED# * Sin(45))
            Else
                Scroll(0.0,WIZARD_SPEED# * Sin(45))
            End If
            
            If SCROLL_X# >= SCROLL_LIMIT_X Or WIZARD_SCREEN_X# < SCROLL_POSITION_RIGHT#
                TranslateWizardScreen(WIZARD_SPEED# * Cos(45),0.0)
            Else
                Scroll(WIZARD_SPEED# * Cos(45),0.0)
            End If
        End If
    
    Else If KeyDown(KEY_UP)
    
        WIZARD_DIRECTION = 1
        MOTION_DIRECTION = MOTION_DIRECTION_NORTH
        
        TranslateWizard(0.0,-WIZARD_SPEED#)
        
        If Not ResolveWallCollisions()
        
            If SCROLL_Y# = 0.0 Or WIZARD_SCREEN_Y# > SCROLL_POSITION_UP#
                TranslateWizardScreen(0.0,-WIZARD_SPEED#)
            Else
                Scroll(0.0,-WIZARD_SPEED)
            End If
        End If
    
    Else If KeyDown(KEY_LEFT)
    
        WIZARD_DIRECTION = 3
        MOTION_DIRECTION = MOTION_DIRECTION_WEST
        
        TranslateWizard(-WIZARD_SPEED#,0.0)
        If Not ResolveWallCollisions()
            If SCROLL_X# = 0.0 Or WIZARD_SCREEN_X# > SCROLL_POSITION_LEFT#
                TranslateWizardScreen(-WIZARD_SPEED#,0.0)
            Else
                Scroll(-WIZARD_SPEED#,0.0)
            End If
        End If
    Else If KeyDown(KEY_RIGHT)
    
        WIZARD_DIRECTION = 2
        MOTION_DIRECTION = MOTION_DIRECTION_EAST
        
        TranslateWizard(WIZARD_SPEED#,0.0)
        If Not ResolveWallCollisions()
            If SCROLL_X# >= SCROLL_LIMIT_X Or WIZARD_SCREEN_X# < SCROLL_POSITION_RIGHT
                TranslateWizardScreen(WIZARD_SPEED#,0.0)
            Else
                Scroll(WIZARD_SPEED#,0.0)
            End If
        End If
    
    Else If KeyDown(KEY_DOWN)
    
        WIZARD_DIRECTION = 0
        MOTION_DIRECTION = MOTION_DIRECTION_SOUTH
        
        TranslateWizard(0.0,WIZARD_SPEED#)
        If Not ResolveWallCollisions()
            If SCROLL_Y# >= SCROLL_LIMIT_Y Or WIZARD_SCREEN_Y# < SCROLL_POSITION_DOWN
                TranslateWizardScreen(0.0,WIZARD_SPEED#)
            Else
                Scroll(0.0,WIZARD_SPEED)
            End If
        End If
    
    End If
    
    wizard_sector_x = Int(WIZARD_X#) / 32
    wizard_sector_y = Int(WIZARD_Y#) / 32
    
    For y% = wizard_sector_y% - 2 To wizard_sector_y% + 2
        For x% = wizard_sector_x% - 2 To wizard_sector_x% + 2
            If x% >= 0 And x% < MAP_WIDTH And y% >= 0 And y% < MAP_HEIGHT
                o = MAP(x%,y%)\occupants
                i = 0
                While i < MAP(x%,y%)\num_occupants% And dead_again% = False
                    If o\id% = ENTITY_ID_GHOST
                        If o\frame% < 3
                            ghost_frame% = o\frame%
                        Else
                            ghost_frame% = 1
                        End If	     
                    
                    If ImagesCollide(IMG_WIZARD,WIZARD_X,WIZARD_Y,WIZARD_DIRECTION * 2 + WIZARD_FRAME,IMG_GHOST,o\x,o\y,ghost_frame%)
                        If WIZARD_HEALTH# > 0.0
                            WIZARD_HEALTH# = WIZARD_HEALTH# - 0.125
                            RemoveEntityFromMap(o)
                            Delete o
                            dead_again% = True
                            PlaySound(SND_GHOST)
                            Exit
                        Else
                            Print "UR DEAD COS LIKE, THE GHOSTS GOT U."
                            End
                        End If
                        
                    End If
                    
                    Else If o\id% = ENTITY_ID_RED_KEY
                        If ImagesCollide(IMG_WIZARD,WIZARD_X,WIZARD_Y,WIZARD_DIRECTION * 2 + WIZARD_FRAME,IMG_RED_KEY,o\x,o\y,0)
                            WIZARD_NUM_RED_KEYS = WIZARD_NUM_RED_KEYS + 1
                            RemoveEntityFromMap(o)
                            Delete o
                            MAP(x%,y%)\id% = 0
                            dead_again% = True
                            Exit
                        End If
                        
                    Else If o\id% = ENTITY_ID_GREEN_KEY
                    
                        If ImagesCollide(IMG_WIZARD,WIZARD_X,WIZARD_Y,WIZARD_DIRECTION * 2 + WIZARD_FRAME,IMG_GREEN_KEY,o\x,o\y,0)
                    
                            WIZARD_NUM_GREEN_KEYS = WIZARD_NUM_GREEN_KEYS + 1
                            RemoveEntityFromMap(o)
                            Delete o
                            MAP(x%,y%)\id% = 0
                            dead_again% = True
                            Exit
                            
                        End If
                    End If
                    i = i + 1
                    o = After o
                Wend
                If dead_again% Then Exit
            End If
        Next
    
        If dead_again% Then Exit
    
    Next	

End Function

Function Shoot()
    Local sector_x%,sector_y%
    If KeyDown(KEY_SPACE)
        If CURRENT_TIME - SHOOT_TIME >= 250
            PlaySound(SND_FIREBALL)
            SHOOT_TIME = CURRENT_TIME
            sector_x% = Int(WIZARD_X#) / 32
            sector_y% = Int(WIZARD_Y#) / 32
            
            Select MOTION_DIRECTION
                Case MOTION_DIRECTION_NORTH
                    AddEntityToMap(CreateFireball(WIZARD_X# + 23,WIZARD_Y# + 25,MAP(sector_x%,sector_y%),270.0),sector_x%,sector_y%)
                Case MOTION_DIRECTION_NORTH_EAST
                    AddEntityToMap(CreateFireball(WIZARD_X# + 23,WIZARD_Y# + 25,MAP(sector_x% + 1,sector_y%),315.0),sector_x%,sector_y%)
                Case MOTION_DIRECTION_EAST
                    AddEntityToMap(CreateFireball(WIZARD_X# + 48,WIZARD_Y# + 21,MAP(sector_x% + 1,sector_y%),0.0),sector_x% + 1,sector_y%)
                Case MOTION_DIRECTION_SOUTH_EAST
                    AddEntityToMap(CreateFireball(WIZARD_X# + 43,WIZARD_Y# + 21,MAP(sector_x% + 1,sector_y%),45.0),sector_x% + 1,sector_y%)
                Case MOTION_DIRECTION_SOUTH
                    AddEntityToMap(CreateFireball(WIZARD_X# + 43,WIZARD_Y# + 21,MAP(sector_x% + 1,sector_y%),90.0),sector_x% + 1,sector_y%)
                Case MOTION_DIRECTION_SOUTH_WEST
                    AddEntityToMap(CreateFireball(WIZARD_X# + 43,WIZARD_Y# + 21,MAP(sector_x% + 1,sector_y%),135.0),sector_x% + 1,sector_y%)
                Case MOTION_DIRECTION_WEST
                    AddEntityToMap(CreateFireball(WIZARD_X# + 15,WIZARD_Y# + 21,MAP(sector_x% + 1,sector_y%),180.0),sector_x% + 1,sector_y%)
                Case MOTION_DIRECTION_NORTH_WEST
                    AddEntityToMap(CreateFireball(WIZARD_X# + 23,WIZARD_Y# + 25,MAP(sector_x%,sector_y%),225.0),sector_x%,sector_y%)
            End Select
        
        End If
    End If
End Function

Function ResolveWallCollisions()
    Local wizard_sector_x,wizard_sector_y,x,y
    
    wizard_sector_x = Int(WIZARD_X#) / 32
    wizard_sector_y = Int(WIZARD_Y#) / 32
    
    For y = wizard_sector_y - 5 To wizard_sector_y + 5
        For x = wizard_sector_x - 5 To wizard_sector_x + 5
            If x >= 0 And x < MAP_WIDTH And y >= 0 And y < MAP_HEIGHT
            
                If MAP(x,y)\id% > 0
                
                    If ImagesCollide(IMG_WIZARD,WIZARD_X#,WIZARD_Y#,WIZARD_DIRECTION * 2 + WIZARD_FRAME,TILES(MAP(x,y)\id%),32 * x,32 * y,0)
                        WIZARD_X# = WIZARD_X# - WIZARD_TRANSLATION_X#
                        WIZARD_Y# = WIZARD_Y# - WIZARD_TRANSLATION_Y#
                        Return 1
                    End If
                ElseIf MAP(x,y)\id% = ENTITY_ID_RED_DOOR_HORIZ
                    
                    If ImageRectCollide(IMG_WIZARD,WIZARD_X#,WIZARD_Y#,WIZARD_DIRECTION * 2 + WIZARD_FRAME,32 * x,32 * y,32 * 5,32)
                        If WIZARD_NUM_RED_KEYS
                            WIZARD_NUM_RED_KEYS = WIZARD_NUM_RED_KEYS - 1
                            RemoveEntityFromMap(MAP(x,y)\occupants)
                            Delete MAP(x,y)\occupants
                            MAP(x,y)\id = 0
                            Return 0
                        Else
                            WIZARD_X# = WIZARD_X# - WIZARD_TRANSLATION_X#
                            WIZARD_Y# = WIZARD_Y# - WIZARD_TRANSLATION_Y#
                            Return 1
                        End If
                    End If
                ElseIf MAP(x,y)\id% = ENTITY_ID_GREEN_DOOR_HORIZ
                    
                    If ImageRectCollide(IMG_WIZARD,WIZARD_X#,WIZARD_Y#,WIZARD_DIRECTION * 2 + WIZARD_FRAME,32 * x,32 * y,32 * 5,32)
                        If WIZARD_NUM_GREEN_KEYS
                            WIZARD_NUM_GREEN_KEYS = WIZARD_NUM_GREEN_KEYS - 1
                            RemoveEntityFromMap(MAP(x,y)\occupants)
                            Delete MAP(x,y)\occupants
                            MAP(x,y)\id = 0
                            Return 0
                        Else
                            WIZARD_X# = WIZARD_X# - WIZARD_TRANSLATION_X#
                            WIZARD_Y# = WIZARD_Y# - WIZARD_TRANSLATION_Y#
                            Return 1
                        End If
                    End If
                ElseIf MAP(x,y)\id% = ENTITY_ID_RED_DOOR_VERT
                    
                    If ImageRectCollide(IMG_WIZARD,WIZARD_X#,WIZARD_Y#,WIZARD_DIRECTION * 2 + WIZARD_FRAME,32 * x,32 * y,32,32 * 5)
                        If WIZARD_NUM_RED_KEYS
                            WIZARD_NUM_RED_KEYS = WIZARD_NUM_RED_KEYS - 1
                            RemoveEntityFromMap(MAP(x,y)\occupants)
                            Delete MAP(x,y)\occupants
                            MAP(x,y)\id = 0
                            Return 0
                        Else
                            WIZARD_X# = WIZARD_X# - WIZARD_TRANSLATION_X#
                            WIZARD_Y# = WIZARD_Y# - WIZARD_TRANSLATION_Y#
                            Return 1
                        End If
                    End If
                ElseIf MAP(x,y)\id% = ENTITY_ID_GREEN_DOOR_VERT
                    
                    If ImageRectCollide(IMG_WIZARD,WIZARD_X#,WIZARD_Y#,WIZARD_DIRECTION * 2 + WIZARD_FRAME,32 * x,32 * y,32,32 * 5)
                        If WIZARD_NUM_GREEN_KEYS
                            WIZARD_NUM_GREEN_KEYS = WIZARD_NUM_GREEN_KEYS - 1
                            RemoveEntityFromMap(MAP(x,y)\occupants)
                            Delete MAP(x,y)\occupants
                            MAP(x,y)\id = 0
                            Return 0
                        Else
                            WIZARD_X# = WIZARD_X# - WIZARD_TRANSLATION_X#
                            WIZARD_Y# = WIZARD_Y# - WIZARD_TRANSLATION_Y#
                            Return 1
                        End If
                    End If
                    
                ElseIf MAP(x,y)\id% = ENTITY_ID_EXIT
                
                    If Abs(WIZARD_X - 32 * x) < 16 And Abs(WIZARD_X - 32 * x) >= 0 And Abs(WIZARD_Y - 32 * y) < 16 And Abs(WIZARD_Y - 32 * y) >= 0
                        Print "YAY U LIKE GOT 2 THE EXIT W/O GETTIN KILLED N STUFF."
                        End
                    End If

                End If
            End If
        Next
    Next
    
    Return 0

End Function

Function Scroll(x#,y#)
    SCROLL_X# = SCROLL_X# + x#
    SCROLL_Y# = SCROLL_Y# + y#
    TILES_REMAINDER_X = (SCROLL_X# * SCALE#) Mod (64 * SCALE#)
    TILES_REMAINDER_Y = (SCROLL_Y# * SCALE#) Mod (64 * SCALE#)
End Function

Function TranslateWizard(x#,y#)
    WIZARD_X# = WIZARD_X# + x#
    WIZARD_Y# = WIZARD_Y# + y#
    WIZARD_TRANSLATION_X# = x#
    WIZARD_TRANSLATION_Y# = y#
End Function

Function TranslateWizardScreen(x#,y#)
    WIZARD_SCREEN_X# = WIZARD_SCREEN_X# + x#
    WIZARD_SCREEN_Y# = WIZARD_SCREEN_Y# + y#
End Function

Function ClampScrolling()

    If SCROLL_X# < 0.0
        SCROLL_X# = 0.0
    End If
    
    If SCROLL_Y# < 0.0
        SCROLL_Y# = 0.0
    End If
    
    If SCROLL_X# >= SCROLL_LIMIT_X
        SCROLL_X# = SCROLL_LIMIT_X
    End If
    
    If SCROLL_Y# >= SCROLL_LIMIT_Y
        SCROLL_Y# = SCROLL_LIMIT_Y
    End If
    
End Function

Function DrawTiles()
    For x = 0 To GRAPHICS_WIDTH / (64 * SCALE#) + 1
        For y = 0 TO GRAPHICS_HEIGHT / (64 * SCALE#) + 1
            SetAlpha 0.25
            DrawImage FLOOR_TILES,x * (64 * SCALE#) - TILES_REMAINDER_X,y * (64 * SCALE#) - TILES_REMAINDER_Y
            SetAlpha 1.0
        Next
    Next
End Function

Function DrawMap()
    
    Local o.Entity
    
    offset_x = Int(SCROLL_X#) / 32
    offset_y = Int(SCROLL_Y#) / 32
    
    remainder_x = Int(SCROLL_X#) Mod 32
    remainder_y = Int(SCROLL_Y#) Mod 32
    
    For y = 0 To NUM_VISIBLE_SECTORS_Y
    
        For x = 0 to NUM_VISIBLE_SECTORS_X
            If x + offset_x < MAP_WIDTH And y + offset_y < MAP_HEIGHT
                If MAP(x + offset_x,y + offset_y)\id% > 0
                    DrawImage TILES(MAP(x + offset_x,y + offset_y)\id%),x * (32 * SCALE#) - remainder_x * SCALE#, y * (32 * SCALE#) - remainder_y * SCALE#
                End If
            End if
        Next
    Next
    
    For y = -5 To NUM_VISIBLE_SECTORS_Y
        For x = -5 to NUM_VISIBLE_SECTORS_X
            If x + offset_x < MAP_WIDTH And y + offset_y < MAP_HEIGHT And x + offset_x >= 0 And y + offset_y >= 0
    
                If MAP(x + offset_x,y + offset_y)\num_occupants <> 0
                
                    o = MAP(x + offset_x,y + offset_y)\occupants
                    num_occupants = MAP(x + offset_x,y + offset_y)\num_occupants% - 1
                    i = 0
                    While i <= num_occupants
    
                        Select o\id%
                            Case ENTITY_ID_FIREBALL
                                SetAlpha(Rnd(0.6,1.0))
                                num_drawn = num_drawn + 1
                                Color 255,255,255
                                DrawImage IMG_FIREBALL,o\x# * SCALE# - SCROLL_X * SCALE#,o\y# * SCALE# - SCROLL_Y * SCALE#
                                setAlpha 1.0
                                
                            Case ENTITY_ID_GENERATOR
                                DrawImage IMG_GENERATOR,o\x * SCALE# - SCROLL_X * SCALE#,o\y * SCALE# - SCROLL_Y * SCALE#,o\frame%
                            
                            Case ENTITY_ID_GHOST
                                If o\frame% < 3
                                    ghost_frame% = o\frame%
                                Else
                                    ghost_frame% = 1
                                End If
                                    SetAlpha 0.75
                                    DrawImage IMG_GHOST,o\x * SCALE# - SCROLL_X * SCALE#,o\y * SCALE# - SCROLL_Y * SCALE#,ghost_frame%
                                    SetAlpha 1.0
                            
                            Case ENTITY_ID_GREEN_KEY
                                    DrawImage IMG_GREEN_KEY,o\x * SCALE# - SCROLL_X * SCALE#,o\y * SCALE# - SCROLL_Y * SCALE#,0
                            
                            Case ENTITY_ID_RED_KEY
                                    DrawImage IMG_RED_KEY,o\x * SCALE# - SCROLL_X * SCALE#,o\y * SCALE# - SCROLL_Y * SCALE#,0
                            
                            Case ENTITY_ID_RED_DOOR_HORIZ
                                SetAlpha 0.5
                                SetGradientColor(255,0,0,0,0,0)
                                GradientHoriz o\x * SCALE# - SCROLL_X * SCALE#,o\y * SCALE# - SCROLL_Y * SCALE#,32 * 5 * SCALE#,32,0,0
                                SetAlpha 1.0
                            
                            Case ENTITY_ID_GREEN_DOOR_HORIZ
                                SetAlpha 0.5
                                SetGradientColor(0,255,0,0,0,0)
                                GradientHoriz o\x * SCALE# - SCROLL_X * SCALE#,o\y * SCALE# - SCROLL_Y * SCALE#,32 * 5 * SCALE#,32,0,0
                                SetAlpha 1.0

                            Case ENTITY_ID_RED_DOOR_VERT
                                SetAlpha 0.5
                                SetGradientColor(255,0,0,0,0,0)
                                GradientVert o\x * SCALE# - SCROLL_X * SCALE#,o\y * SCALE# - SCROLL_Y * SCALE#,32 * 5 * SCALE#,32,0,0
                                SetAlpha 1.0

                            Case ENTITY_ID_GREEN_DOOR_VERT
                                SetAlpha 0.5
                                SetGradientColor(0,255,0,0,0,0)
                                GradientVert o\x * SCALE# - SCROLL_X * SCALE#,o\y * SCALE# - SCROLL_Y * SCALE#,32 * 5 * SCALE#,32,0,0
                                SetAlpha 1.0
                            
                            Case ENTITY_ID_EXIT
                                Color 0,0,0
                                Rect o\x * SCALE# - SCROLL_X * SCALE#,o\y * SCALE# - SCROLL_Y * SCALE#,64,64,True
                                Color 255,255,255
                                sx# = GetScaleX()
                                sy# = GetScaleY()
                                SetScale 0.5 * SCALE#,0.5  * SCALE#
                                Text o\x * SCALE# - SCROLL_X * SCALE# + 32 * SCALE#,o\y * SCALE# - SCROLL_Y * SCALE# + 32 * SCALE#,"EXIT",True,True
                                SetScale sx,sy
                                
                        End Select
                        
                        o = After o
                        i = i + 1
                        
                    Wend
                
                End If
            End if
        Next
    Next

End Function

Function DrawHud()
    Color 128,128,128
    SetAlpha 0.6
    Rect GRAPHICS_WIDTH - HUD_WIDTH * SCALE#,0,HUD_WIDTH,HUD_HEIGHT,True
    SetAlpha 1.0
    x# = Float(HUD_WIDTH) / ImageWidth(IMG_HEALTH_BAR)
    y# = Float(HUD_HEIGHT / 3) / ImageHeight(IMG_HEALTH_BAR)
    
    SetScale x# * SCALE#,y# * SCALE#
    DrawImageRect IMG_HEALTH_BAR,GRAPHICS_WIDTH - HUD_WIDTH * SCALE#,0,0,0,ImageWidth(IMG_HEALTH_BAR) * WIZARD_HEALTH#,ImageHeight(IMG_HEALTH_BAR)	 
    
    SetScale SCALE#,SCALE#
    
    Color 0,0,0
    Rect GRAPHICS_WIDTH - HUD_WIDTH * SCALE#,0,HUD_WIDTH * SCALE#,2 * SCALE#,True
    Rect GRAPHICS_WIDTH - HUD_WIDTH * SCALE#,0,2 * SCALE#,HUD_HEIGHT,True
    Rect GRAPHICS_WIDTH - HUD_WIDTH * SCALE#,HUD_HEIGHT * SCALE# - 2 * SCALE#,HUD_WIDTH * SCALE#,2 * SCALE#,True
    Rect GRAPHICS_WIDTH - 4 * SCALE#,0,4 * SCALE#,HUD_HEIGHT,True
    Text GRAPHICS_WIDTH - 4 * SCALE# - FONT_WIDTH * 8 * SCALE#,0.333 * HUD_HEIGHT * SCALE#,ZeroPad(Str(SCORE),8)
    
    key_scale# = Float(FONT_WIDTH) / ImageWidth(IMG_RED_KEY)
    
    SetScale key_scale# * SCALE#,key_scale# * SCALE#
    
    DrawImage IMG_RED_KEY, GRAPHICS_WIDTH - 4 * SCALE# - FONT_WIDTH * 7.5 * SCALE#,0.666 * HUD_HEIGHT * SCALE# + 0.5 * FONT_WIDTH * SCALE#
    DrawImage IMG_GREEN_KEY, GRAPHICS_WIDTH - 4 * SCALE# - FONT_WIDTH * 3.5 * SCALE#,0.666 * HUD_HEIGHT * SCALE# + 0.5 * FONT_WIDTH * SCALE# 
    
    SetScale SCALE#,SCALE#
    Text GRAPHICS_WIDTH - 4 * SCALE# - FONT_WIDTH * 8 * SCALE#,0.666 * HUD_HEIGHT * SCALE#,"  " + WIZARD_NUM_RED_KEYS + "   " + WIZARD_NUM_GREEN_KEYS

End Function

Function ZeroPad$(MyString$,amount)
    While Len(MyString$) < amount
        MyString = "0" + MyString
    Wend
    Return MyString
End Function

Function DrawWizard()
    DrawImage(IMG_WIZARD,WIZARD_SCREEN_X# * SCALE#,WIZARD_SCREEN_Y# * SCALE#,WIZARD_DIRECTION * 2 + WIZARD_FRAME)
End Function

Function GradientHoriz(x,y,w,h,cx = False,cy = False)
    Local i,j
    Local Offset
    Local Length
    Local HalfLength#
    Local Time
    Local Elapsed
    Local Phase#,Factor#
    
    Time = CURRENT_TIME
    Elapsed = Time - G_PREV_TIME
    
    Phase# = (Float(Elapsed Mod G_UPDATE_TIME) / G_UPDATE_TIME)
    
    If Elapsed >= G_UPDATE_TIME
        G_PREV_TIME = Time - (Elapsed Mod G_UPDATE_TIME)
    EndIf
    
    Length = w
    
    If cx = True
        x = x - w / 2
    EndIf
    
    If cy = True
        y = y - h / 2
    EndIf
    
    HalfLength# = Float(Length) / 2
    
    For i = 0 To Length - 1	
        If (i - (Length * Phase#)) <= 0.0
            j = Length +  (i - (Length * Phase#))
        Else
            j = (i - (Length * Phase#))
        EndIf
        
        If Float(j) / Length >= 0.5
            Factor# = 1.0 - (j - HalfLength#) / HalfLength#		
        Else
            Factor# = j / HalfLength#
        EndIf
        
        Color G_R1 + Factor# * G_RDIFF,G_G1 + Factor# * G_GDIFF,G_B1 + Factor# * G_BDIFF
        
        Rect x + Offset,y,1,h,True
        Offset = Offset + 1
    Next
    
    Return 1
    
End Function

Function GradientVert(x,y,w,h,cx = False,cy = False)
    Local i,j
    Local Offset
    Local Length
    Local HalfLength#
    Local Time
    Local Elapsed
    Local Phase#,Factor#
    
    Time = CURRENT_TIME
    Elapsed = Time - G_PREV_TIME
    
    Phase# = (Float(Elapsed Mod G_UPDATE_TIME) / G_UPDATE_TIME)
    
    If Elapsed >= G_UPDATE_TIME
        G_PREV_TIME = Time - (Elapsed Mod G_UPDATE_TIME)
    EndIf
    
    Length = w
    
    If cx = True
        x = x - w / 2
    EndIf
    
    If cy = True
        y = y - h / 2
    EndIf
    
    
    HalfLength# = Float(Length) / 2
    
    For i = 0 To Length - 1
    
    If (i - (Length * Phase#)) <= 0.0
        j = Length +  (i - (Length * Phase#))
    Else
        j = (i - (Length * Phase#))
    EndIf
    
    If Float(j) / Length >= 0.5
        Factor# = 1.0 - (j - HalfLength#) / HalfLength#		
    Else
        Factor# = j / HalfLength#
    EndIf
    
    Color G_R1 + Factor# * G_RDIFF,G_G1 + Factor# * G_GDIFF,G_B1 + Factor# * G_BDIFF
    
    Rect x,y + Offset,h,1,True
    Offset = Offset + 1
    Next
    
    Return 1
    
End Function

Function SetGradientColor(R1,G1,B1,R2,G2,B2)
    G_R1 = R1
    G_G1 = G1
    G_B1 = B1
    
    G_R2 = R2
    G_G2 = G2
    G_B2 = B2
    
    G_RDIFF = G_R2 - G_R1
    G_GDIFF = G_G2 - G_G1
    G_BDIFF = G_B2 - G_B1
End Function

.MapData

Data 03,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01
Data 03,02,02,02,02,02,02,03,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,03
Data 03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,-9,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,00,00,00,04,03,00,00,-5,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,00,00,00,04,03,00,00,00,00,00,04,03,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,-7,00,00,00,00,04,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,-7,00,00,00,00,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,03
Data 03,00,00,00,00,00,04,03,00,00,00,00,00,04,03,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,03,00,00,00,00,00,04,03,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,-7,00,00,00,00,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,03
Data 03,00,00,00,00,00,04,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,-1,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03

Data 03,00,00,00,00,00,04,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,00,00,00,04,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,03
Data 03,00,00,00,00,00,04,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,-4,00,00,00,00,00,-5,00,03,00,00,00,00,00,04,03,00,00,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,03
Data 03,00,00,00,00,00,04,03,00,00,00,00,00,04,03,00,00,00,-1,00,00,00,00,00,00,-1,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,-1,00,00,00,00,00,00,00,00,00,00,00,00,00,-1,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,00,00,00,04,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,00,00,00,04,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,00,00,04,03
Data 03,00,00,00,00,00,04,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,02,00,00,04,03
Data 03,00,00,00,00,00,04,03,00,00,00,00,00,04,03,00,00,00,00,-1,00,00,00,00,00,-1,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,00,00,00,04,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,00,00,00,02,02,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,-1,00,00,00,00,00,00,00,00,04,03

Data 03,00,00,00,00,00,00,00,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,-1,00,00,00,00,00,00,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,-1,00,00,00,00,00,00,00,00,00,00,00,-1,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,-1,00,00,00,00,00,00,00,00,00,04,03,00,00,00,00,-1,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,-1,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,-5,00,00,00,00,00,00,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,-1,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,00,00,00,00,00,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,-6,00,00,00,00,01,01,01,01,01,01,01,01,01,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,01,01,01,01,01,01,01,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,02,02,02,02,02,02,03,00,00,00,00,00,00,00,00,-1,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,-10,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03

Data 03,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,00,00,00,03,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,-1,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,-5,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,00,00,00,03,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,-1,00,00,00,00,00,00,00,03,00,00,00,00,00,00,00,00,03,00,00,00,00,00,00,00,00,00,00,03,-1,00,-1,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,-7,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,00,00,00,00,00,00,00,00,-9,00,00,00,-1,00,00,00,00,-8,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,0,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,-1,00,-1,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,00,-1,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,00,-5,00,00,00,00,00,00,00,00,00,00,00,00,-1,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,-4,00,-1,00,00,04,03,00,00,00,00,00,00,00,-1,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,-1,00,00,00,00,00,00,00,00,-5,00,00,04,03
Data 03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,03,00,00,00,00,00,04,03,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,04,03
Data 03,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,01,03
