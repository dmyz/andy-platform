import { Modal, message } from 'antdv-next';

export const toast = message;

export function confirmAction(content: string, title = '确认操作') {
  return new Promise<boolean>((resolve) => {
    Modal.confirm({
      title,
      content,
      okText: '确认',
      cancelText: '取消',
      onOk: () => {
        resolve(true);
      },
      onCancel: () => {
        resolve(false);
      },
    });
  });
}
