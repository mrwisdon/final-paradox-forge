param(
    [string]$MapFunctionsRoot = (Join-Path $PSScriptRoot '..\..\..\test\Final_Paradox_v1.1.15\datapacks\luisb1202-functions\data\luisb1202\functions'),
    [string]$OutputFile = (Join-Path $PSScriptRoot '..\..\src\main\java\io\github\finalparadox\client\MarawTharH1BodyFrames.java')
)

$ErrorActionPreference = 'Stop'
$culture = [System.Globalization.CultureInfo]::InvariantCulture
$sourceRoot = [System.IO.Path]::GetFullPath($MapFunctionsRoot)
$outputPath = [System.IO.Path]::GetFullPath($OutputFile)
$h1Root = Join-Path $sourceRoot 'bossfight\b9\h1'
$partNames = @('Head', 'Body', 'LeftArm', 'RightArm', 'LeftLeg', 'RightLeg')
$expectedFrameCounts = @(25, 26, 11, 12, 21)
$expectedTotalTicks = @(37, 35, 24, 28, 45)

function Parse-Number([string]$text) {
    return [double]::Parse($text.Trim().TrimEnd('f'), $culture)
}

function Java-Float([double]$value) {
    if ([Math]::Abs($value) -lt 0.0000000001) {
        return '0.0F'
    }
    return $value.ToString('0.################', $culture) + 'F'
}

function New-Frame {
    $parts = [ordered]@{}
    foreach ($partName in $partNames) {
        $parts[$partName] = @(0.0, 0.0, 0.0)
    }
    return [PSCustomObject]@{
        Parts = $parts
        HoldTicks = 1
    }
}

function Read-ComboFrames([string]$sourceFile) {
    $frames = [System.Collections.Generic.List[object]]::new()
    $activeFrame = $null
    foreach ($line in Get-Content -LiteralPath $sourceFile) {
        if ($line -match 'function luisb1202:bossfight/b9/h1/cuerpo/gen_frame(?:_doble)?$') {
            $activeFrame = New-Frame
            $frames.Add($activeFrame)
            continue
        }
        if ($null -eq $activeFrame) {
            continue
        }
        if ($line -match 'Pose (?:set|merge) value \{(.+)\}$') {
            $poseText = $Matches[1]
            foreach ($match in [regex]::Matches($poseText, '([A-Za-z]+):\[([^\]]+)\]')) {
                $partName = $match.Groups[1].Value
                if ($partNames -notcontains $partName) {
                    continue
                }
                $values = $match.Groups[2].Value.Split(',') |
                        ForEach-Object { Parse-Number $_ }
                if ($values.Count -ne 3) {
                    throw "Expected three pose angles for $partName in $sourceFile"
                }
                $activeFrame.Parts[$partName] = @($values[0], $values[1], $values[2])
            }
            continue
        }
        if ($line -match 'scoreboard players add @e\[tag=b9_h1_as_last\] b4_espada_cd (\d+)') {
            $activeFrame.HoldTicks += [int]$Matches[1]
        }
    }
    return $frames
}

$comboFrames = [System.Collections.Generic.List[object]]::new()
for ($combo = 1; $combo -le 5; $combo++) {
    $sourceFile = Join-Path $h1Root "combo$combo\gen_espada_frame.mcfunction"
    $frames = @(Read-ComboFrames $sourceFile)
    $totalTicks = ($frames.HoldTicks | Measure-Object -Sum).Sum
    if ($frames.Count -ne $expectedFrameCounts[$combo - 1]) {
        throw "Unexpected combo$combo body frame count: $($frames.Count)"
    }
    if ($totalTicks -ne $expectedTotalTicks[$combo - 1]) {
        throw "Unexpected combo$combo body duration: $totalTicks"
    }
    $comboFrames.Add($frames)
}

$builder = [System.Text.StringBuilder]::new()
[void]$builder.AppendLine('package io.github.finalparadox.client;')
[void]$builder.AppendLine()
[void]$builder.AppendLine('import io.github.finalparadox.entity.MarawTharBossEntity;')
[void]$builder.AppendLine('import net.minecraft.util.Mth;')
[void]$builder.AppendLine()
[void]$builder.AppendLine('/**')
[void]$builder.AppendLine(' * Literal body poses and hold durations from the five B9 h1 sword combos.')
[void]$builder.AppendLine(' * Regenerate with tools/reconstruction/generate_marawthar_h1_body_frames.ps1.')
[void]$builder.AppendLine(' */')
[void]$builder.AppendLine('final class MarawTharH1BodyFrames {')
[void]$builder.AppendLine('    private MarawTharH1BodyFrames() {}')
[void]$builder.AppendLine()
[void]$builder.AppendLine('    record Part(float x, float y, float z) {}')
[void]$builder.AppendLine('    record Frame(Part head, Part body, Part leftArm, Part rightArm,')
[void]$builder.AppendLine('                 Part leftLeg, Part rightLeg, int holdTicks) {}')
[void]$builder.AppendLine()

for ($comboIndex = 0; $comboIndex -lt $comboFrames.Count; $comboIndex++) {
    [void]$builder.AppendLine("    private static final Frame[] COMBO_$($comboIndex + 1) = {")
    foreach ($frame in $comboFrames[$comboIndex]) {
        $partValues = [System.Collections.Generic.List[string]]::new()
        foreach ($partName in $partNames) {
            $angles = $frame.Parts[$partName] |
                    ForEach-Object { Java-Float $_ }
            $partValues.Add("new Part($($angles -join ', '))")
        }
        [void]$builder.AppendLine(
                "            new Frame($($partValues -join ', '), $($frame.HoldTicks)),")
    }
    [void]$builder.AppendLine('    };')
    [void]$builder.AppendLine()
}

[void]$builder.AppendLine('    static boolean supports(int animation) {')
[void]$builder.AppendLine('        return animation == MarawTharBossEntity.ANIMATION_TRIPLE_SLASH')
[void]$builder.AppendLine('                || animation == MarawTharBossEntity.ANIMATION_H1_COMBO2')
[void]$builder.AppendLine('                || animation == MarawTharBossEntity.ANIMATION_H1_COMBO3')
[void]$builder.AppendLine('                || animation == MarawTharBossEntity.ANIMATION_H1_COMBO4')
[void]$builder.AppendLine('                || animation == MarawTharBossEntity.ANIMATION_BLUE_RUSH;')
[void]$builder.AppendLine('    }')
[void]$builder.AppendLine()
[void]$builder.AppendLine('    static Frame sample(int animation, float score) {')
[void]$builder.AppendLine('        Frame[] frames = framesFor(animation);')
[void]$builder.AppendLine('        float time = Math.max(0.0F, score - 1.0F);')
[void]$builder.AppendLine('        float cursor = 0.0F;')
[void]$builder.AppendLine('        for (int index = 0; index < frames.length; index++) {')
[void]$builder.AppendLine('            Frame current = frames[index];')
[void]$builder.AppendLine('            float end = cursor + current.holdTicks();')
[void]$builder.AppendLine('            if (time < end || index == frames.length - 1) {')
[void]$builder.AppendLine('                Frame next = frames[Math.min(index + 1, frames.length - 1)];')
[void]$builder.AppendLine('                float blend = MarawTharFrameInterpolation.blend(')
[void]$builder.AppendLine('                        time - cursor, current.holdTicks());')
[void]$builder.AppendLine('                return lerp(current, next, blend);')
[void]$builder.AppendLine('            }')
[void]$builder.AppendLine('            cursor = end;')
[void]$builder.AppendLine('        }')
[void]$builder.AppendLine('        return frames[frames.length - 1];')
[void]$builder.AppendLine('    }')
[void]$builder.AppendLine()
[void]$builder.AppendLine('    private static Frame[] framesFor(int animation) {')
[void]$builder.AppendLine('        return switch (animation) {')
[void]$builder.AppendLine('            case MarawTharBossEntity.ANIMATION_H1_COMBO2 -> COMBO_2;')
[void]$builder.AppendLine('            case MarawTharBossEntity.ANIMATION_H1_COMBO3 -> COMBO_3;')
[void]$builder.AppendLine('            case MarawTharBossEntity.ANIMATION_H1_COMBO4 -> COMBO_4;')
[void]$builder.AppendLine('            case MarawTharBossEntity.ANIMATION_BLUE_RUSH -> COMBO_5;')
[void]$builder.AppendLine('            default -> COMBO_1;')
[void]$builder.AppendLine('        };')
[void]$builder.AppendLine('    }')
[void]$builder.AppendLine()
[void]$builder.AppendLine('    private static Frame lerp(Frame from, Frame to, float delta) {')
[void]$builder.AppendLine('        return new Frame(')
[void]$builder.AppendLine('                lerp(from.head(), to.head(), delta),')
[void]$builder.AppendLine('                lerp(from.body(), to.body(), delta),')
[void]$builder.AppendLine('                lerp(from.leftArm(), to.leftArm(), delta),')
[void]$builder.AppendLine('                lerp(from.rightArm(), to.rightArm(), delta),')
[void]$builder.AppendLine('                lerp(from.leftLeg(), to.leftLeg(), delta),')
[void]$builder.AppendLine('                lerp(from.rightLeg(), to.rightLeg(), delta),')
[void]$builder.AppendLine('                from.holdTicks());')
[void]$builder.AppendLine('    }')
[void]$builder.AppendLine()
[void]$builder.AppendLine('    private static Part lerp(Part from, Part to, float delta) {')
[void]$builder.AppendLine('        return new Part(')
[void]$builder.AppendLine('                angularLerp(from.x(), to.x(), delta),')
[void]$builder.AppendLine('                angularLerp(from.y(), to.y(), delta),')
[void]$builder.AppendLine('                angularLerp(from.z(), to.z(), delta));')
[void]$builder.AppendLine('    }')
[void]$builder.AppendLine()
[void]$builder.AppendLine('    private static float angularLerp(float from, float to, float delta) {')
[void]$builder.AppendLine('        return from + Mth.wrapDegrees(to - from) * delta;')
[void]$builder.AppendLine('    }')
[void]$builder.AppendLine('}')

[System.IO.Directory]::CreateDirectory([System.IO.Path]::GetDirectoryName($outputPath)) | Out-Null
[System.IO.File]::WriteAllText(
        $outputPath, $builder.ToString(), [System.Text.UTF8Encoding]::new($false))
Write-Output "Generated $outputPath from $($expectedFrameCounts -join ', ') body frames."
