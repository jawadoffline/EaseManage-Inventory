import { useState } from 'react';
import { Button, Modal, Upload, Table, Progress, message, Space } from 'antd';
import { DownloadOutlined, UploadOutlined, InboxOutlined } from '@ant-design/icons';
import type { UploadFile } from 'antd';
import apiClient from '../api/client';
import { productsApi } from '../api/products';
import type { ProductRequest } from '../api/products';

interface CsvRow {
  key: number;
  name: string;
  description: string;
  unitOfMeasure: string;
  minStockLevel: number;
  maxStockLevel: number | null;
  reorderPoint: number;
  costPrice: number;
  sellingPrice: number;
  barcode: string;
  isActive: boolean;
}

function parseCsvLine(line: string): string[] {
  const result: string[] = [];
  let current = '';
  let inQuotes = false;

  for (let i = 0; i < line.length; i++) {
    const char = line[i];
    if (inQuotes) {
      if (char === '"' && line[i + 1] === '"') {
        current += '"';
        i++;
      } else if (char === '"') {
        inQuotes = false;
      } else {
        current += char;
      }
    } else {
      if (char === '"') {
        inQuotes = true;
      } else if (char === ',') {
        result.push(current.trim());
        current = '';
      } else {
        current += char;
      }
    }
  }
  result.push(current.trim());
  return result;
}

function parseCsv(text: string): CsvRow[] {
  const lines = text.split('\n').filter((l) => l.trim().length > 0);
  if (lines.length < 2) return [];

  // Skip header row
  const rows: CsvRow[] = [];
  for (let i = 1; i < lines.length; i++) {
    const cols = parseCsvLine(lines[i]);
    if (cols.length < 2) continue;

    // Expected columns: SKU, Name, Description, Category, Unit, Min Stock, Max Stock, Reorder Point, Cost Price, Selling Price, Barcode, Active
    rows.push({
      key: i,
      name: cols[1] || '',
      description: cols[2] || '',
      unitOfMeasure: cols[4] || 'PCS',
      minStockLevel: parseInt(cols[5], 10) || 0,
      maxStockLevel: cols[6] ? parseInt(cols[6], 10) : null,
      reorderPoint: parseInt(cols[7], 10) || 0,
      costPrice: parseFloat(cols[8]) || 0,
      sellingPrice: parseFloat(cols[9]) || 0,
      barcode: cols[10] || '',
      isActive: cols[11] ? cols[11].toLowerCase() === 'true' : true,
    });
  }
  return rows;
}

const previewColumns = [
  { title: 'Name', dataIndex: 'name', key: 'name' },
  { title: 'Description', dataIndex: 'description', key: 'description', ellipsis: true },
  { title: 'Unit', dataIndex: 'unitOfMeasure', key: 'unitOfMeasure', width: 80 },
  { title: 'Min Stock', dataIndex: 'minStockLevel', key: 'minStockLevel', width: 90 },
  { title: 'Cost', dataIndex: 'costPrice', key: 'costPrice', width: 90 },
  { title: 'Sell Price', dataIndex: 'sellingPrice', key: 'sellingPrice', width: 90 },
  { title: 'Barcode', dataIndex: 'barcode', key: 'barcode', width: 120 },
];

export default function ProductImportExport() {
  const [importModalOpen, setImportModalOpen] = useState(false);
  const [csvRows, setCsvRows] = useState<CsvRow[]>([]);
  const [importing, setImporting] = useState(false);
  const [importProgress, setImportProgress] = useState(0);
  const [exporting, setExporting] = useState(false);

  const handleExport = async () => {
    setExporting(true);
    try {
      const response = await apiClient.get('/products/export/csv', {
        responseType: 'blob',
      });
      const blob = new Blob([response.data], { type: 'text/csv' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'products.csv';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      message.success('Products exported successfully');
    } catch {
      message.error('Failed to export products');
    } finally {
      setExporting(false);
    }
  };

  const handleFileRead = (file: UploadFile) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const text = e.target?.result as string;
      const rows = parseCsv(text);
      if (rows.length === 0) {
        message.warning('No valid rows found in CSV');
        return;
      }
      setCsvRows(rows);
      message.success(`Parsed ${rows.length} rows from CSV`);
    };
    reader.readAsText(file as unknown as Blob);
    return false; // Prevent default upload
  };

  const handleImportConfirm = async () => {
    if (csvRows.length === 0) return;

    setImporting(true);
    setImportProgress(0);

    let success = 0;
    let failed = 0;

    for (let i = 0; i < csvRows.length; i++) {
      const row = csvRows[i];
      const request: ProductRequest = {
        name: row.name,
        description: row.description || undefined,
        unitOfMeasure: row.unitOfMeasure,
        minStockLevel: row.minStockLevel,
        maxStockLevel: row.maxStockLevel,
        reorderPoint: row.reorderPoint,
        costPrice: row.costPrice,
        sellingPrice: row.sellingPrice,
        barcode: row.barcode || undefined,
        isActive: row.isActive,
      };

      try {
        await productsApi.create(request);
        success++;
      } catch {
        failed++;
      }

      setImportProgress(Math.round(((i + 1) / csvRows.length) * 100));
    }

    setImporting(false);
    message.success(`Import complete: ${success} created, ${failed} failed`);
    setCsvRows([]);
    setImportModalOpen(false);
    setImportProgress(0);
  };

  const handleCancelImport = () => {
    setImportModalOpen(false);
    setCsvRows([]);
    setImportProgress(0);
  };

  return (
    <>
      <Space>
        <Button
          icon={<DownloadOutlined />}
          onClick={handleExport}
          loading={exporting}
        >
          Export CSV
        </Button>
        <Button
          icon={<UploadOutlined />}
          onClick={() => setImportModalOpen(true)}
        >
          Import CSV
        </Button>
      </Space>

      <Modal
        title="Import Products from CSV"
        open={importModalOpen}
        onCancel={handleCancelImport}
        width={900}
        footer={[
          <Button key="cancel" onClick={handleCancelImport} disabled={importing}>
            Cancel
          </Button>,
          <Button
            key="import"
            type="primary"
            onClick={handleImportConfirm}
            loading={importing}
            disabled={csvRows.length === 0}
          >
            Import {csvRows.length} Products
          </Button>,
        ]}
      >
        {csvRows.length === 0 ? (
          <Upload.Dragger
            accept=".csv"
            showUploadList={false}
            beforeUpload={handleFileRead}
          >
            <p className="ant-upload-drag-icon">
              <InboxOutlined />
            </p>
            <p className="ant-upload-text">
              Click or drag a CSV file here to upload
            </p>
            <p className="ant-upload-hint">
              Expected columns: SKU, Name, Description, Category, Unit, Min Stock,
              Max Stock, Reorder Point, Cost Price, Selling Price, Barcode, Active
            </p>
          </Upload.Dragger>
        ) : (
          <>
            {importing && (
              <Progress
                percent={importProgress}
                status="active"
                style={{ marginBottom: 16 }}
              />
            )}
            <Table
              dataSource={csvRows}
              columns={previewColumns}
              size="small"
              pagination={{ pageSize: 10 }}
              scroll={{ y: 400 }}
            />
          </>
        )}
      </Modal>
    </>
  );
}
