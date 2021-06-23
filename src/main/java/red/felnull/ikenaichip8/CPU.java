package red.felnull.ikenaichip8;

import java.util.Arrays;
import java.util.Random;

public class CPU {
    private final Random random = new Random();
    private final Chip8 chip8;
    private final int[] V = new int[16];
    private int I;
    private final int[] stack = new int[16];
    private int sp;
    private int pc;
    private int delayTimer;
    private int soundTimer;

    protected CPU(Chip8 chip8) {
        this.chip8 = chip8;
    }

    protected void cycle() {
        int opcode = (chip8.memory.read(pc) << 8 | chip8.memory.read(pc + 1));

        execute(opcode);

        if (delayTimer > 0)
            delayTimer--;

        if (soundTimer > 0)
            soundTimer--;
    }

    protected void init() {
        Arrays.fill(this.V, 0);
        this.I = 0;
        Arrays.fill(this.stack, 0);
        this.sp = 0;
        this.pc = 0x200;
        this.delayTimer = 0;
        this.soundTimer = 0;
    }

    private void execute(int opcode) {
        ProcessorCode code = ProcessorCode.getByOpcode(opcode);
        int nextPc = this.pc + 2;
        switch (code) {
            case _00E0:
                Arrays.fill(chip8.graphic, false);
                chip8.drawFlag = true;
                break;
            case _00EE:
                sp--;
                nextPc = stack[sp] + 2;
                break;
            case _1NNN:
                nextPc = opcode & 0x0FFF;
                break;
            case _2NNN:
                stack[sp] = pc;
                sp++;
                nextPc = opcode & 0x0FFF;
                break;
            case _3XNN:
                if (V[(opcode & 0x0F00) >> 8] == (opcode & 0x00FF))
                    nextPc = this.pc + 4;
                break;
            case _4XNN:
                if (V[(opcode & 0x0F00) >> 8] != (opcode & 0x00FF))
                    nextPc = this.pc + 4;
                break;
            case _5XY0:
                if (V[(opcode & 0x0F00) >> 8] == V[(opcode & 0x00F0) >> 4])
                    nextPc = this.pc + 4;
                break;
            case _6XNN:
                V[(opcode & 0x0F00) >> 8] = (opcode & 0x00FF);
                break;
            case _7XNN:
                V[(opcode & 0x0F00) >> 8] += opcode;
                V[(opcode & 0x0F00) >> 8] &= 0x00FF;
                break;
            case _8XY0:
                V[(opcode & 0x0F00) >> 8] = V[(opcode & 0x00F0) >> 4];
                break;
            case _8XY1:
                V[(opcode & 0x0F00) >> 8] |= V[(opcode & 0x00F0) >> 4];
                break;
            case _8XY2:
                V[(opcode & 0x0F00) >> 8] &= V[(opcode & 0x00F0) >> 4];
                break;
            case _8XY3:
                V[(opcode & 0x0F00) >> 8] ^= V[(opcode & 0x00F0) >> 4];
                break;
            case _8XY4:
                if (V[(opcode & 0x00F0) >> 4] > (0xFF - V[(opcode & 0x0F00) >> 8]))
                    V[0xF] = 1;
                else
                    V[0xF] = 0;
                V[(opcode & 0x0F00) >> 8] += V[(opcode & 0x00F0) >> 4];
                break;
            case _8XY5:
                if (V[(opcode & 0x00F0) >> 4] > V[(opcode & 0x0F00) >> 8])
                    V[0xF] = 0;
                else
                    V[0xF] = 1;
                V[(opcode & 0x0F00) >> 8] -= V[(opcode & 0x00F0) >> 4];

                break;
            case _8XY6:
                V[0xF] = (V[(opcode & 0x0F00) >> 8] & 0x1);
                V[(opcode & 0x0F00) >> 8] = V[(opcode & 0x0F00) >> 8] >> 1;
                break;
            case _8XY7:
                if (V[(opcode & 0x0F00) >> 8] > V[(opcode & 0x00F0) >> 4])
                    V[0xF] = 0;
                else
                    V[0xF] = 1;
                V[(opcode & 0x0F00) >> 8] = (V[(opcode & 0x00F0) >> 4] - V[(opcode & 0x0F00) >> 8]);
                break;
            case _8XYE:
                V[0xF] = (V[(opcode & 0x0F00) >> 8] >> 7);
                V[(opcode & 0x0F00) >> 8] = V[(opcode & 0x0F00) >> 8] << 1;
                break;
            case _9XY0:
                if (V[(opcode & 0x0F00) >> 8] != V[(opcode & 0x00F0) >> 4])
                    nextPc = this.pc + 4;
                break;
            case _ANNN:
                I = opcode & 0x0FFF;
                break;
            case _BNNN:
                nextPc = (opcode & 0x0FFF) + V[0];
                break;
            case _CXNN:
                V[(opcode & 0x0F00) >> 8] = (random.nextInt(32767) % 0xFF) & (opcode & 0x00FF);
                break;
            case _DXYN:
                int x = V[(opcode & 0x0F00) >> 8];
                int y = V[(opcode & 0x00F0) >> 4];
                int height = opcode & 0x000F;
                int pixel;

                V[0xF] = 0;
                for (int yline = 0; yline < height; yline++) {
                    pixel = chip8.memory.read(I + yline);
                    for (int xline = 0; xline < 8; xline++) {
                        if ((pixel & (0x80 >> xline)) != 0) {
                            int i = x + xline + ((y + yline) * 64);
                            if (chip8.graphic.length <= i || i < 0)
                                break;
                            if (chip8.graphic[i]) {
                                V[0xF] = 1;
                            }
                            chip8.graphic[i] = !chip8.graphic[i];
                        }
                    }
                }
                chip8.drawFlag = true;
                break;
            case _EX9E:
                if (chip8.key[V[(opcode & 0x0F00) >> 8]])
                    nextPc = this.pc + 4;
                break;
            case _EXA1:
                if (!chip8.key[V[(opcode & 0x0F00) >> 8]])
                    nextPc = this.pc + 4;
                break;
            case _FX07:
                V[(opcode & 0x0F00) >> 8] = delayTimer;
                break;
            case _FX0A:
                boolean keyPress = false;
                for (int i = 0; i < 16; ++i) {
                    if (chip8.key[i]) {
                        V[(opcode & 0x0F00) >> 8] = i;
                        keyPress = true;
                    }
                }
                if (!keyPress)
                    nextPc = this.pc;
                break;
            case _FX15:
                delayTimer = V[(opcode & 0x0F00) >> 8];
                break;
            case _FX18:
                soundTimer = V[(opcode & 0x0F00) >> 8];
                break;
            case _FX1E:
                if (I + V[(opcode & 0x0F00) >> 8] > 0xFFF)
                    V[0xF] = 1;
                else
                    V[0xF] = 0;
                I += V[(opcode & 0x0F00) >> 8];
                break;
            case _FX29:
                I = V[(opcode & 0x0F00) >> 8] * 0x5;
                break;
            case _FX33:
                chip8.memory.write(I, V[(opcode & 0x0F00) >> 8] / 100);
                chip8.memory.write(I + 1, (V[(opcode & 0x0F00) >> 8] / 10) % 10);
                chip8.memory.write(I + 2, (V[(opcode & 0x0F00) >> 8] % 100) % 10);
                break;
            case _FX55:
                System.arraycopy(V, 0, chip8.memory.memory, I, ((opcode & 0x0F00) >> 8) + 1);
                I += ((opcode & 0x0F00) >> 8) + 1;
                break;
            case _FX65:
                System.arraycopy(chip8.memory.memory, I, V, 0, ((opcode & 0x0F00) >> 8) + 1);
                I += ((opcode & 0x0F00) >> 8) + 1;
                break;
        }
        V[(opcode & 0x0F00) >> 8] &= 0xFF;
        this.pc = nextPc;
    }

    protected boolean isBeeping() {
        return soundTimer > 0;
    }

    private enum ProcessorCode {
        //  _0NNN(0x0000, 0x0FFF),
        _00E0(0x00E0),
        _00EE(0x00EE),
        _1NNN(0x1000, 0x0FFF),
        _2NNN(0x2000, 0x0FFF),
        _3XNN(0x3000, 0x0FFF),
        _4XNN(0x4000, 0x0FFF),
        _5XY0(0x5000, 0x0FF0),
        _6XNN(0x6000, 0x0FFF),
        _7XNN(0x7000, 0x0FFF),
        _8XY0(0x8000, 0x0FF0),
        _8XY1(0x8001, 0x0FF0),
        _8XY2(0x8002, 0x0FF0),
        _8XY3(0x8003, 0x0FF0),
        _8XY4(0x8004, 0x0FF0),
        _8XY5(0x8005, 0x0FF0),
        _8XY6(0x8006, 0x0FF0),
        _8XY7(0x8007, 0x0FF0),
        _8XYE(0x800E, 0x0FF0),
        _9XY0(0x9000, 0x0FF0),
        _ANNN(0xA000, 0x0FFF),
        _BNNN(0xB000, 0x0FFF),
        _CXNN(0xC000, 0x0FFF),
        _DXYN(0xD000, 0x0FFF),
        _EX9E(0xE09E, 0x0F00),
        _EXA1(0xE0A1, 0x0F00),
        _FX07(0xF007, 0x0F00),
        _FX0A(0xF00A, 0x0F00),
        _FX15(0xF015, 0x0F00),
        _FX18(0xF018, 0x0F00),
        _FX1E(0xF01E, 0x0F00),
        _FX29(0xF029, 0x0F00),
        _FX33(0xF033, 0x0F00),
        _FX55(0xF055, 0x0F00),
        _FX65(0xF065, 0x0F00);
        private final int code;
        private final int uncde;

        ProcessorCode(int code) {
            this(code, 0x0000);
        }

        ProcessorCode(int code, int uncde) {
            this.code = code;
            this.uncde = uncde;
        }

        private static ProcessorCode getByOpcode(int opcode) {
            for (ProcessorCode value : values()) {
                int oncode = opcode & ~value.uncde;
                if (value.code == oncode)
                    return value;
            }
            throw new IllegalStateException("Non existent processor code: " + Integer.toHexString(opcode));
        }
    }
}
