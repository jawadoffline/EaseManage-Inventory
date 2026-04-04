import { describe, it, expect, vi } from 'vitest';
import { exportToCsv } from './csvExport';

describe('exportToCsv', () => {
  it('should create CSV with headers and rows', () => {
    const createObjectURL = vi.fn(() => 'blob:url');
    const revokeObjectURL = vi.fn();
    global.URL.createObjectURL = createObjectURL;
    global.URL.revokeObjectURL = revokeObjectURL;

    const click = vi.fn();
    vi.spyOn(document, 'createElement').mockReturnValue({
      set href(_: string) {},
      set download(_: string) {},
      click
    } as any);

    exportToCsv('test.csv', ['Name', 'Age'], [['Alice', 30], ['Bob', 25]]);

    expect(createObjectURL).toHaveBeenCalled();
    expect(click).toHaveBeenCalled();
    expect(revokeObjectURL).toHaveBeenCalled();
  });

  it('should handle null values', () => {
    const createObjectURL = vi.fn(() => 'blob:url');
    const revokeObjectURL = vi.fn();
    global.URL.createObjectURL = createObjectURL;
    global.URL.revokeObjectURL = revokeObjectURL;

    const click = vi.fn();
    vi.spyOn(document, 'createElement').mockReturnValue({
      set href(_: string) {},
      set download(_: string) {},
      click
    } as any);

    exportToCsv('test.csv', ['Name'], [[null], [undefined], ['']]);

    expect(createObjectURL).toHaveBeenCalled();
  });
});
