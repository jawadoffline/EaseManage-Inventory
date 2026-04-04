import { useRef } from 'react';

interface BarcodeLabelProps {
  sku: string;
  name: string;
  barcode: string;
}

function generateBarPattern(barcode: string): string {
  if (!barcode) return 'transparent';

  const stops: string[] = [];
  const barWidth = 100 / (barcode.length * 2 + 1);
  let position = 0;

  for (let i = 0; i < barcode.length; i++) {
    const charCode = barcode.charCodeAt(i);
    const isBlack = charCode % 2 === 0;
    const color = isBlack ? '#000' : '#fff';
    const nextPosition = position + barWidth;

    stops.push(`${color} ${position}%`);
    stops.push(`${color} ${nextPosition}%`);
    position = nextPosition;

    // Spacing bar (always white)
    const spaceEnd = position + barWidth;
    stops.push(`#fff ${position}%`);
    stops.push(`#fff ${spaceEnd}%`);
    position = spaceEnd;
  }

  return `linear-gradient(90deg, ${stops.join(', ')})`;
}

const labelStyle: React.CSSProperties = {
  width: 300,
  padding: 20,
  border: '1px solid #d9d9d9',
  borderRadius: 8,
  backgroundColor: '#fff',
  fontFamily: 'Arial, sans-serif',
  display: 'inline-block',
};

const nameStyle: React.CSSProperties = {
  fontSize: 14,
  fontWeight: 'bold',
  marginBottom: 8,
  textAlign: 'center',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
};

const skuStyle: React.CSSProperties = {
  fontSize: 12,
  fontFamily: 'monospace',
  color: '#555',
  textAlign: 'center',
  marginBottom: 12,
};

const barContainerStyle: React.CSSProperties = {
  height: 50,
  marginBottom: 8,
  borderRadius: 2,
};

const barcodeTextStyle: React.CSSProperties = {
  fontSize: 16,
  fontFamily: 'monospace',
  letterSpacing: 4,
  textAlign: 'center',
  color: '#222',
};

const printBtnStyle: React.CSSProperties = {
  display: 'block',
  margin: '12px auto 0',
  padding: '4px 16px',
  fontSize: 12,
  cursor: 'pointer',
  border: '1px solid #d9d9d9',
  borderRadius: 4,
  backgroundColor: '#fafafa',
};

const printCss = `
@media print {
  body * { visibility: hidden !important; }
  .barcode-label, .barcode-label * { visibility: visible !important; }
  .barcode-label { position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); }
  .barcode-label-print-btn { display: none !important; }
}
`;

export default function BarcodeLabel({ sku, name, barcode }: BarcodeLabelProps) {
  const labelRef = useRef<HTMLDivElement>(null);

  const handlePrint = () => {
    window.print();
  };

  return (
    <>
      <style>{printCss}</style>
      <div ref={labelRef} className="barcode-label" style={labelStyle}>
        <div style={nameStyle}>{name}</div>
        <div style={skuStyle}>SKU: {sku}</div>
        {barcode && (
          <>
            <div
              style={{
                ...barContainerStyle,
                background: generateBarPattern(barcode),
              }}
            />
            <div style={barcodeTextStyle}>{barcode}</div>
          </>
        )}
        {!barcode && (
          <div style={{ textAlign: 'center', color: '#999', fontSize: 12 }}>
            No barcode assigned
          </div>
        )}
        <button
          className="barcode-label-print-btn"
          style={printBtnStyle}
          onClick={handlePrint}
        >
          Print Label
        </button>
      </div>
    </>
  );
}
